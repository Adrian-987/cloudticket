package com.cloudticket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.AdminMapper;
import com.cloudticket.mapper.AuditLogMapper;
import com.cloudticket.mapper.MerchantMapper;
import com.cloudticket.mapper.UserMapper;
import com.cloudticket.pojo.dto.AuditLogPageQuery;
import com.cloudticket.pojo.entity.AuditLog;
import com.cloudticket.pojo.vo.AuditLogVO;
import com.cloudticket.service.AuditLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper,AuditLog> implements AuditLogService {

    @Autowired
    AuditLogMapper auditLogMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    MerchantMapper merchantMapper;

    @Autowired
    AdminMapper adminMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean save(AuditLog auditLog) {
        return auditLogMapper.insert(auditLog)>0;
    }

    @Override
    public PageResult<AuditLogVO> auditLogPage(AuditLogPageQuery query) {
        //先查库得到log信息
        Page<AuditLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        String action = query.getAction();
        LocalDateTime beginTime = query.getBeginTime();
        Integer operatorType = query.getOperatorType();
        LocalDateTime endTime = query.getEndTime();
        page = lambdaQuery().eq(action != null, AuditLog::getAction, action)
                .eq(operatorType != null, AuditLog::getOperatorType, operatorType)
                .ge(beginTime != null, AuditLog::getCreateTime, beginTime)
                .le(endTime != null, AuditLog::getCreateTime, endTime)
                .page(page);

        // 分页驱动 + 当页批量补充名字
        //得到分3端的id的map
        Map<String, String> names = loadOperatorNames(page.getRecords());
        //分装成vo,并加入名字字段
        List<AuditLogVO> list = page.getRecords().stream()
                .map(log -> toVO(log, names))
                .toList();
        return new PageResult<>((int) page.getTotal(), list);
    }

    /**
     * 批量加载当页操作人名字，返回 Map<"operatorType:operatorId", 名字>。
     * 三组分开收集 id、各做一次 IN 查询，空组跳过。
     */
    private Map<String, String> loadOperatorNames(List<AuditLog> records) {
        Set<Long> userIds = collectIds(records, 1);
        Set<Long> merchantIds = collectIds(records, 2);
        Set<Long> adminIds = collectIds(records, 3);

        Map<String, String> names = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds)
                    .forEach(u -> names.put("1:" + u.getId(), u.getNickname()));
        }
        if (!merchantIds.isEmpty()) {
            merchantMapper.selectBatchIds(merchantIds)
                    .forEach(m -> names.put("2:" + m.getId(), m.getName()));
        }
        if (!adminIds.isEmpty()) {
            adminMapper.selectBatchIds(adminIds)
                    .forEach(a -> names.put("3:" + a.getId(), a.getRealName()));
        }
        return names;
    }

    /** 从当页记录里，按操作人类型收集 id */
    private Set<Long> collectIds(List<AuditLog> records, int operatorType) {
        return records.stream()
                .filter(log -> log.getOperatorType() != null && log.getOperatorType() == operatorType)
                .map(AuditLog::getOperatorId)
                .collect(Collectors.toSet());
    }

    /** 单条日志 → VO：复制同名字段 + 补操作人名字 + 类型描述 */
    private AuditLogVO toVO(AuditLog log, Map<String, String> names) {
        AuditLogVO vo = new AuditLogVO();
        BeanUtils.copyProperties(log, vo);
        vo.setOperatorName(names.get(log.getOperatorType() + ":" + log.getOperatorId()));
        vo.setOperatorTypeDesc(switch (log.getOperatorType()) {
            case 1 -> "用户";
            case 2 -> "主办方";
            case 3 -> "平台";
            default -> "未知";
        });
        return vo;
    }
}
