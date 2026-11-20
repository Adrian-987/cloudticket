package com.cloudticket.service.impl;

import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.context.BaseContext;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.*;
import com.cloudticket.pojo.dto.*;
import com.cloudticket.pojo.entity.*;
import com.cloudticket.pojo.enums.*;
import com.cloudticket.pojo.vo.*;
import com.cloudticket.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    /** 待支付订单有效期（分钟） */
    private static final int PAY_EXPIRE_MINUTES = 15;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    SessionMapper sessionMapper;

    @Autowired
    ShowInfoMapper showInfoMapper;

    @Autowired
    AttendeeMapper attendeeMapper;

    @Autowired
    TicketCategoryMapper ticketCategoryMapper;

    @Autowired
    StockMapper stockMapper;

    @Autowired
    TicketMapper ticketMapper;

    @Autowired
    PaymentMapper paymentMapper;

    @Autowired
    UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCreateOrderVO orderCreate(OrderCreateDTO orderCreateDTO) {
        Long sessionId = orderCreateDTO.getSessionId();

        // ===== ① 场次 + 演出：判空 / 状态 / 时间窗口 =====
        ShowSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException("该场次不存在");
        }
        ShowInfo showInfo = showInfoMapper.selectById(session.getShowId());
        if (showInfo == null) {
            throw new BaseException("该演出不存在");
        }
        if (showInfo.getStatus() != ShowStatus.ON_SALE.getCode()) {
            throw new BaseException("该演出未处于销售中");
        }
        if (session.getStatus() != SessionStatus.ON_SALE.getCode()) {
            throw new BaseException("该场次未处于销售中");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getSaleStartTime()) || now.isAfter(session.getSaleEndTime())) {
            throw new BaseException("未处于该场次售票时间");
        }

        List<OrderItemCreateDTO> items = orderCreateDTO.getItems();

        // ===== ② 拍平 + 本单判重（同一观演人不能重复选择） =====
        List<Long> allAttendeeIds = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (OrderItemCreateDTO item : items) {
            for (Long attendeeId : item.getAttendeeIds()) {
                if (!seen.add(attendeeId)) {
                    throw new BaseException("同一观演人不能重复选择");
                }
                allAttendeeIds.add(attendeeId);
            }
        }

        // ===== ③ 观演人归属（一次查全部，别人的查不出来） =====
        Long currentId = BaseContext.getCurrentId();
        List<Attendee> attendees = attendeeMapper.selectList(
                new LambdaQueryWrapper<Attendee>()
                        .in(Attendee::getId, allAttendeeIds)
                        .eq(Attendee::getUserId, currentId));
        if (attendees.size() != allAttendeeIds.size()) {
            throw new BaseException("观演人不存在");
        }
        Map<Long, Attendee> attendeeMap = attendees.stream()
                .collect(Collectors.toMap(Attendee::getId, a -> a));

        // ===== ④ 证件号判重（两条通讯录同身份证）+ 历史订单判重 =====
        List<String> idCards = attendees.stream().map(Attendee::getIdCardNo).toList();
        if (new HashSet<>(idCards).size() != idCards.size()) {
            throw new BaseException("同一身份证件不能重复购买");
        }
        if (orderItemMapper.selectCounts(sessionId, idCards) > 0) {
            throw new BaseException("该观演人已购买过该场次");
        }

        // ===== ⑤ 票档：存在 / 属于该演出 =====
        List<Long> categoryIds = items.stream()
                .map(OrderItemCreateDTO::getCategoryId)
                .distinct()
                .toList();
        List<TicketCategory> categories = ticketCategoryMapper.selectByIds(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new BaseException("票档不存在");
        }
        Map<Long, TicketCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(TicketCategory::getId, c -> c));
        for (TicketCategory category : categories) {
            if (!category.getShowId().equals(showInfo.getId())) {
                throw new BaseException("票档不属于该演出");
            }
        }

        // ===== ⑥ 限购：按档聚合本单人数（防"同档拆多个 item"绕过） =====
        Map<Long, Integer> currentMap = new HashMap<>();
        for (OrderItemCreateDTO item : items) {
            currentMap.merge(item.getCategoryId(), item.getAttendeeIds().size(), Integer::sum);
        }
        Map<Long, Integer> historyMap = orderItemMapper.selectHistoryCount(sessionId, currentId, categoryIds)
                .stream()
                .collect(Collectors.toMap(CategoryCountVO::getCategoryId, CategoryCountVO::getCnt));
        for (Map.Entry<Long, Integer> entry : currentMap.entrySet()) {
            TicketCategory category = categoryMap.get(entry.getKey());
            int history = historyMap.getOrDefault(entry.getKey(), 0);
            if (history + entry.getValue() > category.getLimitPerUser()) {
                throw new BaseException("票档「" + category.getName() + "」超出限购数量");
            }
        }

        // ===== ⑦ 金额：服务端重算（价格取 DB，不信前端） =====
        BigDecimal amount = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : currentMap.entrySet()) {
            amount = amount.add(categoryMap.get(entry.getKey()).getPrice()
                    .multiply(BigDecimal.valueOf(entry.getValue())));
        }
        BigDecimal payAmount = amount;   // P1 无券，discount = 0

        // ===== ⑧ 扣库存：条件更新（rows=0 → 库存不足） =====
        for (Map.Entry<Long, Integer> entry : currentMap.entrySet()) {
            int rows = stockMapper.saveCount(sessionId, entry.getKey(), entry.getValue());
            if (rows == 0) {
                throw new BaseException("票档「" + categoryMap.get(entry.getKey()).getName() + "」库存不足");
            }
        }

        // ===== ⑨ 订单主表 =====
        Orders order = Orders.builder()
                .orderNo(generateOrderNo())
                .userId(currentId)
                .merchantId(showInfo.getMerchantId())
                .showId(showInfo.getId())
                .sessionId(sessionId)
                .orderType(1)      // 1 演出票
                .poolType(1)       // P1 只走公开池
                .amount(amount)
                .discountAmount(BigDecimal.ZERO)
                .payAmount(payAmount)
                .status(OrderStatus.WAIT_PAY.getCode())
                .expireTime(LocalDateTime.now().plusMinutes(PAY_EXPIRE_MINUTES))
                .build();
        orderMapper.insert(order);

        // ===== ⑩ 订单明细：一行 = 一个观演人（姓名/证件快照） =====
        for (OrderItemCreateDTO item : items) {
            TicketCategory category = categoryMap.get(item.getCategoryId());
            for (Long attendeeId : item.getAttendeeIds()) {
                Attendee attendee = attendeeMap.get(attendeeId);
                orderItemMapper.insert(OrderItem.builder()
                        .orderId(order.getId())
                        .categoryId(item.getCategoryId())
                        .unitPrice(category.getPrice())
                        .attendeeName(attendee.getName())
                        .idCardNo(attendee.getIdCardNo())
                        .build());
            }
        }

        return UserCreateOrderVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .payAmount(payAmount)
                .expireTime(order.getExpireTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderPay(String  orderNo) {
        //1.查询订单归属和状态
        Long currentId = BaseContext.getCurrentId();
        Orders order = orderMapper
                .selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderNo, orderNo));
        if(order==null||!order.getUserId().equals(currentId)){
            throw new BaseException("该订单不存在");
        }
        Integer status = order.getStatus();
        if(status==OrderStatus.CANCELLED.getCode()){
            throw new BaseException("该订单已取消");
        }
        if(status!=OrderStatus.WAIT_PAY.getCode()){
            // 已支付 / 已出票 / 已核销 / 已完成 / 退款中 / 已退款 → 统一视为"已支付过"
            throw new BaseException("该订单已支付");
        }
        //2.修改订单状态为已支付
        Boolean success=lambdaUpdate().set(Orders::getPayTime,LocalDateTime.now())
                .set(Orders::getStatus,OrderStatus.PAID.getCode())
                .eq(Orders::getId,order.getId())
                .eq(Orders::getStatus,OrderStatus.WAIT_PAY.getCode())
                .update();
        if(!success){
            throw new BaseException("订单状态已变更,请刷新");
        }
        //3,创建流水
        Payment mockPay = Payment.builder()
                .outTradeNo("MOCK" + orderNo)
                .orderId(order.getId())
                .amount(order.getPayAmount())
                .status(PaymentStatus.SUCCESS.getCode())
                .payTime(LocalDateTime.now())
                .callbackTime(LocalDateTime.now())
                .callbackBody("MOCK PAY").build();
        paymentMapper.insert(mockPay);
        //3.VIP 虚拟订单：直接完成 + 会员生效（不走出票）
        if (order.getOrderType() != null && order.getOrderType() == OrderType.VIP.getCode()) {
            lambdaUpdate().set(Orders::getStatus, OrderStatus.FINISHED.getCode())
                    .eq(Orders::getId, order.getId()).update();

            VipPlanStatus plan = VipPlanStatus.of(order.getVipPlanCode());
            if (plan == null) {
                throw new BaseException("会员档位不存在");
            }
            User user = userMapper.selectById(order.getUserId());
            LocalDateTime nowTime = LocalDateTime.now();
            LocalDateTime base = (user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(nowTime))
                    ? user.getVipExpireTime() : nowTime;
            user.setVipLevel(1);
            user.setVipExpireTime(base.plusMonths(plan.getMonths()));
            userMapper.updateById(user);
            return;
        }
        //4.创建票次
        List<OrderItem> items = orderItemMapper
                .selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,order.getId()));
        for (OrderItem item : items) {
            Ticket ticket = Ticket.builder()
                    .ticketNo(generateTicketNo())
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .sessionId(order.getSessionId())
                    .categoryId(item.getCategoryId())
                    .price(item.getUnitPrice())
                    .attendeeName(item.getAttendeeName())
                    .idCardNo(item.getIdCardNo())
                    .status(TicketStatus.VALID.getCode())
                    .build();
            ticketMapper.insert(ticket);
        }
        //4.直接出票
        lambdaUpdate().set(Orders::getStatus, OrderStatus.ISSUED.getCode())
                .eq(Orders::getId, order.getId()).eq(Orders::getStatus, OrderStatus.PAID.getCode()).update();
    }

    @Override
    public PageResult<UserOrderVO> orderUserPage(UserOrderPageQuery userOrderPageQuery) {
        Page<UserOrderVO> page=new Page<>(userOrderPageQuery.getPageNum(), userOrderPageQuery.getPageSize());
        orderMapper.selectUserOrderPage(page,BaseContext.getCurrentId(),userOrderPageQuery.getStatus());
        page.getRecords().forEach(userOrderVO -> {
            userOrderVO.setStatusDesc(OrderStatus.of(userOrderVO.getStatus()).getDesc());
        });
        return new PageResult<>((int)page.getTotal(),page.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderCancel(Long id) {
        Boolean success=lambdaUpdate().set(Orders::getStatus,OrderStatus.CANCELLED.getCode())
                .eq(Orders::getId,id)
                .eq(Orders::getStatus,OrderStatus.WAIT_PAY.getCode())
                .eq(Orders::getUserId,BaseContext.getCurrentId())
                .update();
        if (!success){
            throw new BaseException("该订单状态已刷新或该订单不存在");
        }
        Orders orders = getById(id);
        Long sessionId = orders.getSessionId();
        List<CategoryCountVO> list=orderItemMapper.selectCount1(id);
        for (CategoryCountVO categoryCountVO:list){
            int row=stockMapper.cancelCount(sessionId,categoryCountVO.getCategoryId(),categoryCountVO.getCnt());
            if (row == 0) {
                throw new BaseException("库存回补失败");
            }
        }
    }

    @Override
    public MerchantOrderDetailVO selectOrder(Long id) {
        MerchantOrderDetailVO merchantOrderVO=orderMapper.selectMerchantOrder(id,BaseContext.getCurrentId());
        if (merchantOrderVO == null) {
            throw new BaseException("订单不存在");
        }
        merchantOrderVO.setStatusDesc(OrderStatus.of(merchantOrderVO.getStatus()).getDesc());
        // 买家昵称脱敏（契约 8.3：买家信息脱敏）
        if (merchantOrderVO.getBuyer() != null) {
            merchantOrderVO.getBuyer().setNickname(maskNickname(merchantOrderVO.getBuyer().getNickname()));
        }
        List<MerchantTicketVO> merchantTickets=ticketMapper.selectMerchant(id,BaseContext.getCurrentId());
        merchantTickets.forEach(t->{
            t.setStatusDesc(TicketStatus.of(t.getStatus()).getDesc());
        });
        merchantOrderVO.setTickets(merchantTickets);
        return merchantOrderVO;
    }

    @Override
    public PageResult<AdminVipOrderVO> vipOrderPage(OrderVipPageQuery orderVipPageQuery) {
        Page<AdminVipOrderVO> page = new Page<>(orderVipPageQuery.getPageNum(), orderVipPageQuery.getPageSize());
        IPage<AdminVipOrderVO> result = orderMapper.selectAdminVip(page, orderVipPageQuery);
        result.getRecords().forEach(vo -> {
            vo.setStatusDesc(OrderStatus.of(vo.getStatus()).getDesc());
            VipPlanStatus plan = VipPlanStatus.of(vo.getVipPlanCode());
            if (plan != null) {
                vo.setPlanName(plan.getPlanName());
                vo.setMonths(plan.getMonths());
            }
            vo.setPhone(maskPhone(vo.getPhone()));
        });
        return new PageResult<>((int) result.getTotal(), result.getRecords());
    }

    @Override
    public UserCreateVipVO userVipCreate(UserVipCreateDTO userVipCreateDTO) {
        VipPlanStatus status = VipPlanStatus.of(userVipCreateDTO.getPlanCode());
        if (status == null) {
            throw new BaseException("会员档位不存在");
        }
        Orders orders= Orders.builder()
                .orderNo(generateOrderNo())
                .userId(BaseContext.getCurrentId())
                .orderType(OrderType.VIP.getCode())
                .poolType(PoolType.PUBLIC.getCode())
                .amount(status.getPrice())
                .payAmount(status.getPrice())
                .vipPlanCode(userVipCreateDTO.getPlanCode())
                .status(OrderStatus.WAIT_PAY.getCode())
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .build();
        orderMapper.insert(orders);
        UserCreateVipVO userCreateVipVO=new UserCreateVipVO();
        BeanUtils.copyProperties(orders,userCreateVipVO);
        userCreateVipVO.setOrderId(orders.getId());
        return userCreateVipVO;
    }

    @Override
    public UserOrderDetailVO orderDetail(Long orderId) {
        // ① 主查询（订单 + 演出 + 场次，一对一；归属校验）
        UserOrderDetailVO vo = orderMapper.selectUserOrderDetail(orderId, BaseContext.getCurrentId());
        if (vo == null) {
            throw new BaseException("订单不存在");
        }
        vo.setStatusDesc(OrderStatus.of(vo.getStatus()).getDesc());

        // ② 明细：平铺票位 → 按票档聚合
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (orderItems.isEmpty()) {
            vo.setItems(List.of());
        } else {
            //得到票档map
            List<Long> categoryIds = orderItems.stream().map(OrderItem::getCategoryId).distinct().toList();

            List<TicketCategory> ticketCategories = ticketCategoryMapper.selectByIds(categoryIds);
            Map<Long, TicketCategory> categoryMap = ticketCategories.stream()
                    .collect(Collectors.toMap(TicketCategory::getId, c -> c));
            //按照票档分组成map
            Map<Long, List<OrderItem>> byCategory = orderItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getCategoryId));

            List<UserOrderItemVO> list1 = byCategory.entrySet().stream().map(entry -> {
                List<OrderItem> list = entry.getValue();
                TicketCategory category = categoryMap.get(entry.getKey());
                List<String> list2 = list.stream()
                        .map(i -> maskName(i.getAttendeeName()))
                        .toList();
                return UserOrderItemVO.builder()
                        .categoryName(category == null ? null : category.getName())
                        .unitPrice(list.getFirst().getUnitPrice())
                        .quantity(list.size())
                        .attendeeNames(list2)
                        .build();
            }).toList();
            vo.setItems(list1);
        }

        // ③ 票（未支付为空数组；出票后返回）
        List<Ticket> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getOrderId, orderId));
        vo.setTickets(tickets.stream().map(t -> UserTicketVO.builder()
                .ticketNo(t.getTicketNo())
                .status(t.getStatus())
                .statusDesc(TicketStatus.of(t.getStatus()).getDesc())
                .build()).toList());

        return vo;
    }

    /** 姓名脱敏：保留姓，其余变 *（张三 → 张*） */
    private String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    @Override
    public PageResult<MerchantOrderVO> merchantOrderPage(MerchantOrderPageQuery query) {
        Page<MerchantOrderVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        orderMapper.selectMerchantOrderPage(page, BaseContext.getCurrentId(), query);
        page.getRecords().forEach(vo -> {
            vo.setStatusDesc(OrderStatus.of(vo.getStatus()).getDesc());
            vo.setBuyerNickname(maskNickname(vo.getBuyerNickname()));
        });
        return new PageResult<>((int) page.getTotal(), page.getRecords());
    }

    /** 昵称脱敏：保留首字 + ***（小星 → 小***） */
    private String maskNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return nickname;
        }
        return nickname.charAt(0) + "***";
    }

    /** 手机号脱敏：138****1234 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 订单号：yyyyMMddHHmmss + 6 位随机（uk_order_no 唯一索引兜底幂等） */
    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
    private String generateTicketNo() {
        return "TK"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}
