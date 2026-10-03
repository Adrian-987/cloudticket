# 云票 CloudTicket · 票务预订平台

面向演出场景的双端票务交易平台（主办方端 + 用户端），以**开售抢票**为核心高并发场景：实名限购、双池库存（公开池/会员池）、延迟取消、扫码核销、退票退款全闭环。

> 个人企业级学习项目 · 简历项目 · 全程按"问题 → 方案 → 验证实验 → 数据"的方式推进

## 技术栈

| 层 | 选型 |
|---|---|
| 基础 | Java 21（虚拟线程压测实验）· Spring Boot 3.5.x · Maven 多模块 |
| 持久层 | MyBatis-Plus（单表）+ MyBatis XML（复杂 SQL）· MySQL 8 |
| 缓存 | Redis 7 + Redisson + Caffeine 多级缓存 |
| 消息队列 | RocketMQ 5.x（异步削峰 / 延迟消息） |
| 鉴权 | 拦截器 + 自写 JWT（v1）→ Sa-Token（v2，演进点） |
| 防护 | Sentinel 限流熔断降级 |
| 实时通信 | WebSocket（来单提醒 / 抢票播报站） |
| 文档与工具 | Knife4j · EasyExcel · JMeter · Docker Compose |
| 支付 | 支付宝沙箱（RSA2 验签 + 异步回调 + 退款） |

## 仓库结构（Monorepo）

```
cloudticket
├── backend/                 后端 · Maven 聚合工程
│   ├── cloudticket-common   通用工具 / 统一返回 / 异常基类 / 请求上下文
│   ├── cloudticket-pojo     Entity / DTO / VO
│   └── cloudticket-server   启动类 · Controller · Service · Mapper · 配置
├── frontend/                前端（web-user / web-admin，随 P1 接入）
└── docs/                    项目文档（大纲 / SQL / 接口契约）
```

## 快速开始

```bash
# 环境要求：JDK 21、MySQL 8、Redis 7、Maven 3.9+
# 1. 初始化数据库
mysql -uroot -p < docs/sql/001_cloudticket_p1_schema.sql
# 2. 配置连接（后续提供 application-local.yml 模板）
# 3. 构建并启动（在 backend/ 下执行）
cd backend
mvn clean install -DskipTests
cd cloudticket-server && mvn spring-boot:run
# 4. 接口文档：http://localhost:8080/doc.html
```

## 文档索引

| 文档 | 说明 |
|---|---|
| [票务系统项目大纲.md](./docs/票务系统项目大纲.md) | 产品需求 + 技术方案 + 六期迭代路线（项目宪法） |
| [docs/sql/](./docs/sql/) | 建表 SQL（按期分文件） |
| [docs/api/](./docs/api/) | apifox 导出的接口文档 |

## 里程碑（2026-10-02 启动，允许 ±1 周弹性）

| 阶段 | 内容 | 预计完成 | 状态 |
|---|---|---|---|
| P1 | 骨架与双端业务闭环（CRUD/状态机/数据权限/审计） | 2026-10-16 | 🚧 进行中 |
| P2 | 读优化与可观测（缓存三件套/多级缓存/压测基线/traceId） | 2026-10-27 | ⏳ |
| P3 | 抢票高并发（Lua 双池/RocketMQ 异步/超时三重保障/支付宝沙箱） | 2026-11-14 | ⏳ |
| P4 | 可靠性与实时（本地消息表/对账/Sentinel/WebSocket/EasyExcel） | 2026-12-01 | ⏳ |
| P5 | 社区与玩法（帖子/点赞/评论/签到 Bitmap/积分兑换） | 2026-12-12 | ⏳ |
| P6 | 加分与交付（ES/部署上线/README 终版/简历） | 2026-12-24 | ⏳ |

## 提交规范

`<type>: <摘要>`，type 取值：`feat` 功能 / `fix` 修复 / `docs` 文档 / `refactor` 重构 / `test` 测试 / `chore` 构建。
示例：`feat: P1 演出审核状态机与条件更新流转`
