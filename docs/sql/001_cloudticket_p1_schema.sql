-- ============================================================================
-- 云票 CloudTicket · P1 数据库表设计（14 张）
-- 环境：MySQL 8.x / InnoDB / utf8mb4
--
-- 设计规范：
--   1. 逻辑外键：不建物理外键约束，一致性由服务层保证（苍穹规范）
--   2. 金额一律 DECIMAL(10,2)，禁用 float/double（浮点误差）
--   3. 审计四字段 create_time/create_user/update_time/update_user，
--      由 @AutoFill AOP 自动填充；audit_log 例外（只追加，只留 create_time）
--   4. 状态字段统一 tinyint + 注释枚举，Java 侧用常量类对应
--   5. 与技术落位挂钩的关键字段以 [落位] 标注
-- ============================================================================

CREATE DATABASE IF NOT EXISTS cloudticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE cloudticket;

-- ----------------------------------------------------------------------------
-- 1. 用户表
-- ----------------------------------------------------------------------------
CREATE TABLE `user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`           VARCHAR(20)  NOT NULL                COMMENT '手机号（登录账号）',
    `password_hash`   VARCHAR(100) NOT NULL                COMMENT '密码摘要（BCrypt，禁明文/MD5）',
    `nickname`        VARCHAR(50)  NULL                    COMMENT '昵称（播报站脱敏展示）',
    `avatar_url`      VARCHAR(255) NULL                    COMMENT '头像地址',
    `vip_level`       TINYINT      NOT NULL DEFAULT 0      COMMENT '会员等级：0普通 1会员 [落位:会员专享池/退票权益]',
    `vip_expire_time` DATETIME     NULL                    COMMENT '会员到期时间',
    `points`          INT          NOT NULL DEFAULT 0      COMMENT '积分余额（冗余值，以 points_log 流水为准，对账校准）',
    `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0禁用 1正常',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`     BIGINT       NULL,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`     BIGINT       NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB COMMENT ='用户表';

-- ----------------------------------------------------------------------------
-- 2. 主办方表
-- ----------------------------------------------------------------------------
CREATE TABLE `merchant` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          VARCHAR(100) NOT NULL                COMMENT '主办方/公司名称',
    `license_no`    VARCHAR(50)  NOT NULL                COMMENT '统一社会信用代码',
    `contact_name`  VARCHAR(50)  NULL                    COMMENT '联系人',
    `contact_phone` VARCHAR(20)  NOT NULL                COMMENT '联系电话（登录名）',
    `password_hash` VARCHAR(100) NOT NULL                COMMENT '密码摘要（BCrypt）',
    `status`        TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0待审核 1正常 2禁用 [落位:入驻审核状态机]',
    `audit_remark`  VARCHAR(255) NULL                    COMMENT '审核备注（驳回原因）',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`   BIGINT       NULL,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`   BIGINT       NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_license_no` (`license_no`),
    UNIQUE KEY `uk_contact_phone` (`contact_phone`)
) ENGINE = InnoDB COMMENT ='主办方表';

-- ----------------------------------------------------------------------------
-- 3. 平台管理员表
-- ----------------------------------------------------------------------------
CREATE TABLE `admin` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(50)  NOT NULL                COMMENT '登录名',
    `password_hash` VARCHAR(100) NOT NULL                COMMENT '密码摘要（BCrypt）',
    `real_name`     VARCHAR(50)  NULL                    COMMENT '姓名',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0禁用 1正常',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`   BIGINT       NULL,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`   BIGINT       NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT ='平台管理员表';

-- ----------------------------------------------------------------------------
-- 4. 场馆表 [落位: P6 可选项 GEO 附近场馆]
-- ----------------------------------------------------------------------------
CREATE TABLE `venue` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100) NOT NULL                COMMENT '场馆名称',
    `city`        VARCHAR(50)  NOT NULL                COMMENT '城市',
    `address`     VARCHAR(255) NOT NULL                COMMENT '详细地址',
    `lng`         DECIMAL(10, 7) NULL                  COMMENT '经度',
    `lat`         DECIMAL(10, 7) NULL                  COMMENT '纬度',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user` BIGINT       NULL,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user` BIGINT       NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_city_name` (`city`, `name`)
) ENGINE = InnoDB COMMENT ='场馆表';

-- ----------------------------------------------------------------------------
-- 5. 演出表
-- ----------------------------------------------------------------------------
CREATE TABLE `show_info` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `merchant_id` BIGINT       NOT NULL                COMMENT '主办方 id [落位:@DataScope 数据权限]',
    `venue_id`    BIGINT       NOT NULL                COMMENT '场馆 id',
    `title`       VARCHAR(128) NOT NULL                COMMENT '演出标题',
    `category`    VARCHAR(32)  NOT NULL                COMMENT '分类：演唱会/话剧/音乐会/脱口秀…',
    `cover_url`   VARCHAR(255) NULL                    COMMENT '封面图（OSS）',
    `detail`      TEXT         NULL                    COMMENT '图文详情（富文本）',
    `guide`       VARCHAR(1024) NULL                   COMMENT '观演指南（入场证件/开场时间/交通指引）',
    `status`      TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0草稿 1待审核 2驳回 3审核通过 4销售中 5已结束 6已下架 [落位:审核状态机+条件更新CAS]',
    `total_stock` INT          NOT NULL DEFAULT 0      COMMENT '总库存（两池之和，冗余校验用）',
    `sold_count`  INT          NOT NULL DEFAULT 0      COMMENT '总销量（冗余，攒批统计+对账校准）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user` BIGINT       NULL,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user` BIGINT       NULL,
    PRIMARY KEY (`id`),
    KEY `idx_merchant` (`merchant_id`),
    KEY `idx_status_category` (`status`, `category`),
    KEY `idx_venue` (`venue_id`)
) ENGINE = InnoDB COMMENT ='演出表';

-- ----------------------------------------------------------------------------
-- 6. 场次表
-- ----------------------------------------------------------------------------
CREATE TABLE `show_session` (
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `show_id`        BIGINT   NOT NULL                COMMENT '演出 id',
    `session_name`   VARCHAR(50) NULL                 COMMENT '场次名（周六晚场）',
    `session_time`   DATETIME NOT NULL                COMMENT '演出开始时间',
    `sale_start_time` DATETIME NOT NULL               COMMENT '开售时间 [落位:开售流转定时任务]',
    `sale_end_time`  DATETIME NOT NULL                COMMENT '停售时间',
    `status`         TINYINT  NOT NULL DEFAULT 0      COMMENT '状态：0未开售 1销售中 2已结束（条件更新CAS流转）',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`    BIGINT   NULL,
    `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`    BIGINT   NULL,
    PRIMARY KEY (`id`),
    KEY `idx_show` (`show_id`),
    KEY `idx_sale_start` (`sale_start_time`)
) ENGINE = InnoDB COMMENT ='场次表';

-- ----------------------------------------------------------------------------
-- 7. 票档表（库存主体）
-- ----------------------------------------------------------------------------
CREATE TABLE `ticket_category` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id`        BIGINT        NOT NULL                COMMENT '场次 id',
    `name`              VARCHAR(50)   NOT NULL                COMMENT '票档名（内场VIP/看台A）',
    `price`             DECIMAL(10, 2) NOT NULL               COMMENT '票价',
    `stock`             INT           NOT NULL DEFAULT 0      COMMENT '公开池库存 [落位:双池Lua扣减]',
    `sold_count`        INT           NOT NULL DEFAULT 0      COMMENT '公开池已售',
    `member_stock`      INT           NOT NULL DEFAULT 0      COMMENT '会员池库存（仅会员可购）',
    `sold_member_count` INT           NOT NULL DEFAULT 0      COMMENT '会员池已售',
    `limit_per_user`    INT           NOT NULL DEFAULT 2      COMMENT '单用户限购数量',
    `version`           INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本号 [落位:MP乐观锁插件/条件更新兜底]',
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`       BIGINT        NULL,
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`       BIGINT        NULL,
    PRIMARY KEY (`id`),
    KEY `idx_session` (`session_id`)
) ENGINE = InnoDB COMMENT ='票档表（双池库存主体）';

-- ----------------------------------------------------------------------------
-- 8. 观演人表 [落位:实名限购的数据基础]
-- ----------------------------------------------------------------------------
CREATE TABLE `attendee` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT      NOT NULL                COMMENT '所属用户',
    `name`        VARCHAR(50) NOT NULL                COMMENT '姓名',
    `id_card_no`  VARCHAR(32) NOT NULL                COMMENT '证件号（演示明文；生产应加密存储+哈希索引）',
    `phone`       VARCHAR(20) NULL                    COMMENT '联系电话',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user` BIGINT      NULL,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user` BIGINT      NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_idcard` (`user_id`, `id_card_no`)
) ENGINE = InnoDB COMMENT ='观演人表';

-- ----------------------------------------------------------------------------
-- 9. 订单表
-- ----------------------------------------------------------------------------
CREATE TABLE `orders` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`        VARCHAR(32)   NOT NULL                COMMENT '业务订单号（雪花/号段，唯一索引兜底幂等）',
    `user_id`         BIGINT        NOT NULL                COMMENT '下单用户',
    `merchant_id`     BIGINT        NOT NULL                COMMENT '主办方（数据权限过滤用）',
    `show_id`         BIGINT        NOT NULL                COMMENT '演出 id',
    `session_id`      BIGINT        NOT NULL                COMMENT '场次 id',
    `order_type`      TINYINT       NOT NULL DEFAULT 1      COMMENT '类型：1演出票 2会员开通（虚拟商品）',
    `pool_type`       TINYINT       NOT NULL DEFAULT 1      COMMENT '扣减池：1公开池 2会员池 [落位:回补按池路由]',
    `amount`          DECIMAL(10, 2) NOT NULL DEFAULT 0     COMMENT '原价总额',
    `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0     COMMENT '券抵扣金额 [落位:金额三段式重算]',
    `pay_amount`      DECIMAL(10, 2) NOT NULL DEFAULT 0     COMMENT '应付金额',
    `coupon_id`       BIGINT        NULL                    COMMENT '使用的用户券（P4）',
    `status`          TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0待支付 1已支付(待出票) 2已出票 3已核销 4已完成 5已取消 6退款中 7已退款 [落位:订单状态机]',
    `expire_time`     DATETIME      NOT NULL                COMMENT '支付截止时间 [落位:延迟消息超时取消]',
    `pay_time`        DATETIME      NULL                    COMMENT '支付时间',
    `out_trade_no`    VARCHAR(32)   NULL                    COMMENT '支付渠道单号（唯一索引→回调幂等）',
    `cancel_reason`   VARCHAR(100)  NULL                    COMMENT '取消/关闭原因',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`     BIGINT        NULL,
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`     BIGINT        NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_merchant_status` (`merchant_id`, `status`),
    KEY `idx_status_expire` (`status`, `expire_time`) COMMENT '超时兜底扫描专用',
    KEY `idx_session` (`session_id`)
) ENGINE = InnoDB COMMENT ='订单表';

-- ----------------------------------------------------------------------------
-- 10. 订单票位明细表（一行 = 一个票位：谁 + 哪档 + 多少钱）
-- ----------------------------------------------------------------------------
CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`      BIGINT        NOT NULL                COMMENT '订单 id',
    `category_id`   BIGINT        NOT NULL                COMMENT '票档 id',
    `unit_price`    DECIMAL(10, 2) NOT NULL               COMMENT '成交单价（快照）',
    `attendee_name` VARCHAR(50)   NOT NULL                COMMENT '观演人姓名快照（下单时固化）',
    `id_card_no`    VARCHAR(32)   NOT NULL                COMMENT '证件号快照 [落位:实名判重，覆盖待支付占用]',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`   BIGINT        NULL,
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`   BIGINT        NULL,
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_idcard` (`id_card_no`)
) ENGINE = InnoDB COMMENT ='订单票位明细表（一行=一张票位）';

-- ----------------------------------------------------------------------------
-- 11. 票表（一票一码）
-- ----------------------------------------------------------------------------
CREATE TABLE `ticket` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ticket_no`     VARCHAR(32) NOT NULL                COMMENT '票号（核销凭证，唯一）',
    `order_id`      BIGINT      NOT NULL                COMMENT '订单 id',
    `user_id`       BIGINT      NOT NULL                COMMENT '持有人（下单账号，票夹归属）',
    `session_id`    BIGINT      NOT NULL                COMMENT '场次 id',
    `category_id`   BIGINT      NOT NULL                COMMENT '票档 id',
    `attendee_name` VARCHAR(50) NOT NULL                COMMENT '观演人姓名快照（出票时从票位复制）',
    `id_card_no`    VARCHAR(32) NOT NULL                COMMENT '证件号快照 [落位:人证合一核验]',
    `status`        TINYINT     NOT NULL DEFAULT 1      COMMENT '状态：1有效 2已核销 3已退款 4已作废 [落位:核销CAS幂等]',
    `check_time`  DATETIME    NULL                    COMMENT '核销时间',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user` BIGINT      NULL,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user` BIGINT      NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_no` (`ticket_no`),
    KEY `idx_order` (`order_id`),
    KEY `idx_session_status` (`session_id`, `status`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB COMMENT ='票表';

-- ----------------------------------------------------------------------------
-- 12. 支付流水表
-- ----------------------------------------------------------------------------
CREATE TABLE `payment` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `out_trade_no`  VARCHAR(32)   NOT NULL                COMMENT '商户单号（唯一）',
    `order_id`      BIGINT        NOT NULL                COMMENT '订单 id',
    `amount`        DECIMAL(10, 2) NOT NULL               COMMENT '支付金额（回调时校验与 pay_amount 一致）',
    `status`        TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0待支付 1成功 2失败 3已退款',
    `pay_time`      DATETIME      NULL                    COMMENT '支付成功时间',
    `callback_time` DATETIME      NULL                    COMMENT '回调到达时间',
    `callback_body` TEXT          NULL                    COMMENT '回调原始报文（对账/排查用）',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`   BIGINT        NULL,
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`   BIGINT        NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB COMMENT ='支付流水表';

-- ----------------------------------------------------------------------------
-- 13. 退款单表
-- ----------------------------------------------------------------------------
CREATE TABLE `refund` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `refund_no`    VARCHAR(32)   NOT NULL                COMMENT '退款单号（唯一，退款幂等）',
    `order_id`     BIGINT        NOT NULL                COMMENT '订单 id',
    `ticket_id`    BIGINT        NULL                    COMMENT '关联票（整单退为 NULL）',
    `amount`       DECIMAL(10, 2) NOT NULL               COMMENT '退款金额',
    `reason`       VARCHAR(255)  NULL                    COMMENT '退款原因',
    `status`       TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0申请中 1审核通过 2退款中 3已退款 4已拒绝',
    `audit_by`     BIGINT        NULL                    COMMENT '审核人 [落位:会员优先审核队列]',
    `audit_time`   DATETIME      NULL                    COMMENT '审核时间',
    `audit_remark` VARCHAR(255)  NULL                    COMMENT '审核备注',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`  BIGINT        NULL,
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user`  BIGINT        NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_order` (`order_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT ='退款单表';

-- ----------------------------------------------------------------------------
-- 14. 审计日志表（只追加，不更新）
-- ----------------------------------------------------------------------------
CREATE TABLE `audit_log` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_id`  BIGINT      NOT NULL                COMMENT '操作人 id',
    `operator_type` TINYINT    NOT NULL                COMMENT '操作人类型：1用户 2主办方 3平台',
    `action`       VARCHAR(50) NOT NULL                COMMENT '动作：REFUND_AUDIT/BATCH_IMPORT/CHECK_IN/…',
    `target_type`  VARCHAR(30) NULL                    COMMENT '目标类型（SHOW/ORDER/REFUND…）',
    `target_id`    BIGINT      NULL                    COMMENT '目标 id',
    `params`       TEXT        NULL                    COMMENT '请求参数快照（脱敏后）',
    `result`       TINYINT     NOT NULL DEFAULT 1      COMMENT '结果：0失败 1成功',
    `ip`           VARCHAR(45) NULL                    COMMENT '操作 IP（兼容 IPv6）',
    `trace_id`     VARCHAR(32) NULL                    COMMENT '链路追踪 id [落位:MDC 可观测性]',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间（追加表无更新字段）',
    PRIMARY KEY (`id`),
    KEY `idx_operator` (`operator_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB COMMENT ='审计日志表';
