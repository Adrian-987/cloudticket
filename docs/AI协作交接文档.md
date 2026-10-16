# 云票 CloudTicket · AI 协作交接文档

> **用途**：交接给后续 AI 助手的上下文压缩包。项目文档（项目大纲 / SQL v1.4 / 表设计说明 / 实体类设计说明 / API 契约 00-07 / README）已齐备且随仓库走，**本文档不重复其内容**，只记录文档之外的对话沉淀：协作规则、口头决议、踩坑记录、当前进度与下一步。新 AI 请先读完本文档，再按需查阅 docs/ 下的正式文档。

---

## 一、用户画像与协作规则（最高优先级，必须遵守）

- **用户画像**：后端学习者，已完整实践 JavaSE / JavaWeb / Spring Boot / SSM / MyBatis-Plus / 黑马 Redis 全套课程；API 和注解学过但遗忘较多，需要"提醒式教学"（陌生的 API 要顺带讲含义和原理）；git / maven / docker 等非代码工程环节由 AI 负责教学和给意见。
- **项目目标**：第一个可写进简历的企业级个人项目，追求技术亮点 + 真实工程能力，讲解需附带**面试视角**。
- **硬性规则**：
  1. **未经用户明确同意，不得修改用户代码**——用户提问时只解答 + 给后续做法建议，不动手；
  2. **未经用户明确同意，不得执行任何 git 操作**（commit / push / mv 都要）——用户会直接说；
  3. 回答以资深全栈工程师身份展开：为什么这么设计、为什么用这个技术、具体代码逻辑怎么走，而不是只给结论；
- **职责边界**：AI 负责 查错 debug / 技术意见 / code review / 前端代码书写 / 工程工具教学；**后端业务代码是用户的学习实践场，AI 只指导不代笔**（基础设施/脚手架类经用户授权可代写，已有先例：枚举、实体由 AI 生成）；
- **代码交付讲解制**：AI 写的代码必须逐文件讲清"是什么 / 解决什么 / 为什么这么设计 / 以后谁用"；
- **沟通**：中文；commit 规范 `<type>: <摘要>`（feat/fix/docs/refactor/test/chore）。

---

## 二、项目概况与文档地图

- **项目**：云票 CloudTicket——演出票务双端平台（主办方端 + 用户端 + 平台审核端），核心场景为**开售抢票高并发**，实名限购（一人一证一场一张）、双池库存（公开池/会员池）、订单超时三重保障、扫码核销。
- **仓库**：Monorepo，`https://github.com/Adrian-987/cloudticket.git`，main 分支。
  ```
  ├── backend/                 Maven 聚合工程（父 pom 统一版本管理）
  │   ├── cloudticket-common   Result / BaseException / BaseContext / PageResult / JwtUtil
  │   ├── cloudticket-pojo     14 实体（entity）+ 8 状态枚举（enums）
  │   └── cloudticket-server   启动类 / config×6 / interceptor / handler / aspect / annotation
  ├── frontend/                占位（P1 后期：Vue3 + Vite + JS + Element Plus + Pinia + Axios，AI 生成）
  └── docs/                    大纲（宪法）/ sql（v1.4 基线 + 002 初始化）/ 表设计说明 v1.4 /
                               实体类设计说明 v1.1 / api 契约（00 规范 + 01-07 共 45 接口）
  ```
- **技术基线**（变更需用户同意）：Java 21 / Spring Boot **3.5.16**（勿升 4.x，生态未适配）/ MyBatis-Plus 3.5.12（单表）+ MyBatis XML（复杂 SQL）/ MySQL 8.0.46（本地已建库导入）/ Redis 7 + Redisson（**P2 才用，暂未引入使用**）/ RocketMQ 5.x（P3）/ jjwt 0.12.6（自写 JWT，P6 换 Sa-Token）/ Knife4j 4.5.0 / Hutool 5.8.38 / Sentinel（P4）/ EasyExcel（P4）/ JMeter / Docker Compose + 支付宝沙箱（P3，PaymentService 策略接口已留 mock 位置）/ Elasticsearch（P6 可选）。**单体多模块，不上微服务**。

---

## 三、当前进度（截至交接）

**✅ 已完成**
- 工程骨架：三模块聚合、统一返回 Result、全局异常（业务 200/校验 400/未知 500）、BaseContext(ThreadLocal)、AutoFill AOP + AuditMetaObjectHandler、乐观锁插件、Knife4j、JacksonConfig（Long→String、LocalDateTime 格式）、PageResult、8 状态枚举、14 实体、JwtProperties/JwtUtil/JwtAuthInterceptor/WebMvcConfig（三区三密钥 + excludes + CORS）；
- 设计全部定稿并经用户审核（详见 docs/）；
- 数据库：本地 MySQL 已建库 `cloudticket` 并执行 001/002（结构 v1.4 + admin 初始账号 + 3 示例场馆）；
- Git：全部已推送 GitHub，工作区干净。

**⏳ 进行中 / 未做（接手者从这里继续）**
1. **JWT 验收三连未跑**：临时 Controller `GET /user/test`（无 token → 401"请先登录"）→ 临时 main 调 `JwtUtil.createToken` 打印 token → 带 Authorization 头访问 → 200 → **删临时接口**；
2. **第一个业务接口：用户注册**（契约见 docs/api/01）：POST /user/auth/register，入参 phone/password/nickname，BCrypt 加密存 password_hash，`uk_phone` 冲突捕获转"该手机号已注册"，注册即签发 token（调 JwtUtil.createToken，claims 放 userId）；
3. 后续顺序：登录 → 观演人 CRUD → 平台端场馆/审核 → 主办方演出管理（状态机 CAS）→ 用户端浏览 → 交易链路（下单 7 步校验链 / 支付 mock / 取消 / 核销）；
4. 未建：DTO/VO（随接口写，不预生成）、service/mapper/controller 包下的业务类；
5. 前端、Redis 使用、RocketMQ、ES：分别按大纲 P1 后期/P2/P3/P6。

---

## 四、关键设计决议（对话达成，部分未完全承载于正式文档）

1. **配置两层风格（用户原设计，勿改）**：主 application.yml 敏感项用**同名占位符**引用（`secret: ${cloudticket.jwt.user.secret}`），真实值只写 application-local.yml（gitignored）；同名自引用兼任**缺失检查**（local 未提供则启动报错，用户有意为之）；datasource 用 `ck.datasource.*` 模板组合。**曾有过一次"改为常规风格"又被用户要求改回的先例，勿再犯**；
2. **JWT 分工**：JwtUtil 在 common（静态、密钥走参数、common 允许第三方依赖）；JwtProperties/拦截器/WebMvcConfig 在 server config/interceptor 包（依赖 Spring 装配）；
3. **拦截器三区三密钥**：user/merchant/admin 各自独立密钥 + 独立 claimKey（userId/merchantId/adminId）——跨区 token 在验签层直接失败（防御纵深，勿合并为一把密钥）；
4. **实体五件套**：@Data / @NoArgsConstructor / @AllArgsConstructor / @Builder / @TableName + Serializable(serialVersionUID=1L) + @TableId(AUTO)；状态字段 Integer 存数字，枚举只做翻译器（不用 @EnumValue）；AuditLog 只追加（仅 createTime，无 update 审计字段，strictFill 天然兼容）；TicketCategory.version 是全项目唯一 @Version；
5. **实名模型**：判重按**证件号**不按账号（order_item 快照，覆盖待支付占用，取消/退款自动释放）；ticket 一行=一个票位（无 quantity 列）；attendee 是"可增删的通讯录"，票上是"不可变快照"，两者解耦不互外键；
6. **演出详情拆两接口**：`GET /user/shows/{id}`（静态，P2 缓存对象）与 `GET /user/sessions/{sessionId}/categories`（动态余量，P3 独立限流）——静态/动态分离是缓存策略的前提；
7. **场馆归属**：平台管理员维护（公共维度资源，无 merchant_id 不在数据权限内），主办方端只读列表引用；删除有前置引用校验；venue 有 uk_city_name 同城不重名；
8. **状态出参成对**：status 数字（逻辑用）+ statusDesc 中文（展示用，枚举 desc 翻译），后端翻译前端零映射；
9. **禁用账户生效语义**：v1 = 登录拦截 + 关键写接口校验 status（JWT 无状态无法立即失效），读接口容忍延迟；v2 = Redis 黑名单立即失效；
10. **接口范围决议**：全平台订单总览 P1 不加（P4 对账时加 /admin/orders/page）；批量导入/统计看板/退票闭环/通知 = P4；社区/签到/积分 = P5；ES/GEO/HLL = P6 可选；
11. **AI 预留**：AiAssistantService 策略接口 + LocalRuleFaqImpl + ai_conversation 表（未建，P6）+ SSE 端点占位；演示方向"AI 总结观演评价"；
12. **演进叙事（面试素材）**：v1 拦截器自写 JWT → v2 Sa-Token；v1 LIKE → v2 ES；单缓存 → 多级缓存；每个演进讲"为什么改、效果如何"。

---

## 五、踩坑与陷阱记录（后来者必读）

1. **Spring Initializr 生成器混版本**：曾按 Boot4 风格生成 `spring-boot-starter-webmvc` / `-test` 后缀 starter 混进 3.5.16 → 构建必炸；已修复，勿复现（Boot 4 的 starter 命名和 Jackson 3 都不同）；
2. **jjwt 0.12 API 与 0.9 完全不同**：`claims()`（非 setClaims）、`signWith(key)`（传 Key 对象，自动按长度选 HS256/384/512）、`parser().verifyWith(key).build().parseSignedClaims(token).getPayload()`（非 setSigningKey/getBody）；secret ≥32 字节否则 `WeakKeyException`；
3. **jjwt claims 数值反序列化可能是 Integer**：`claims.get("userId")` 取出后用 `((Number) x).longValue()` 防强转炸；
4. **拦截器不是 Bean**：手动 new 的对象上 @Value/@Autowired 全部失效；依赖走构造参数中转（WebMvcConfig 注入 JwtProperties → new 拦截器时传值）；拦截器单例无状态，per-request 数据只进 ThreadLocal；
5. **拦截器异常不走 @RestControllerAdvice**：401 手写响应（setStatus + ContentType UTF-8 + Hutool JSONUtil.toJsonStr(Result.error(msg))）；
6. **afterCompletion 必须 removeCurrentId**：Tomcat 线程池复用，不清理 = 身份串号；
7. **用 WebMvcConfigurer**，别继承 WebMvcConfigurationSupport（关闭自动配置，连带 Jackson Customizer 失效）；**不要自定义主 ObjectMapper Bean**（@ConditionalOnMissingBean 退避使 Customizer 全部失效）；
8. **MP 分页插件 3.5.9+ 需额外引入 jsqlparser**——尚未引入，接分页时处理；乐观锁插件已配，仅作用于 MP 内置 update，手写 XML 条件更新不经过它；
9. **MySQL 8 URL 参数**：serverTimezone=Asia/Shanghai、allowPublicKeyRetrieval=true 必带（不配：时区差 8h / 公钥检索报错）；
10. **Maven**：dependencyManagement 只登记版本不下发，子模块 dependencies 引入不写版本；选 Boot 大版本先查第三方生态适配；
11. **git**：嵌套 .gitignore 合法（逐级生效）；.gitignore 行内 `#` 不是注释；**Revert ≠ Undo**（前者改代码留历史，后者留代码删提交）；`force-with-lease` 优于 `--force`；reflog 是 90 天兜底；回退三连 = `reset --hard 回历史` + `cherry-pick -n 捞代码` + `force-with-lease 同步远端`；
12. **Windows 文件锁**：IDEA / 文档查看工具占用目录会导致 git mv 失败（Permission denied），关闭占用者再动；
13. **Maven 同步报"未解析的兄弟模块依赖"**：IDEA 工作区缓存错位，`mvn install` 装本地仓库兜底。

---

## 六、开发工作流约定

- 每完成一个模块：编译（mvn compile）→ apifox/Knife4j 自测 → 对照接口文档验收 → git 提交（docs 与 feat 分开提交）；
- 推送：走本机代理 `git -c http.proxy=http://127.0.0.1:10808 push`（或用户手动在 IDEA 推）；
- **验证实验纪律**：每个技术亮点必须配可执行的验证实验并记录前后对比数字（压测 / EXPLAIN / 故障注入）——这是简历 bullet 的原材料；
- 用户工具链：IDEA（Maven 根 = backend/pom.xml）+ DataGrip（管库）+ apifox（接口调试）。

---

## 七、环境信息

- Windows + Git Bash；JDK 21；Maven（IDEA 内置）；MySQL 8.0.46 本地实例（库 cloudticket；root 账号密码在 application-local.yml，**勿写入任何会提交的文件或对话产出**）；Redis 已装未使用（P2）；Git 推送依赖本机代理 127.0.0.1:10808；
- 用户本地还装有苍穹外卖（sky_take_out）、tlias 等课程库，操作时只动 cloudticket 库。

---

## 八、下一步路线（按序执行）

1. JWT 验收三连（见三-1）；
2. 用户注册 → 登录（首个业务链路，签发 token 闭环）；
3. 观演人 CRUD（最简业务练手，含归属校验 IDOR 防护）；
4. 平台端：场馆管理、主办方/演出审核（CAS + 审计）；
5. 主办方端：演出/场次/票档管理（含双池字段）、订单管理（@DataScope 数据权限）、核销（幂等 CAS）；
6. 用户端交易：下单 7 步校验链（同步扣库存条件更新）→ 支付 mock → 订单/取消（超时扫描）；
7. P1 收尾：EasyExcel 前置的运营接口按大纲属 P4，勿提前；
8. P2 起按大纲推进（缓存三件套 → 多级缓存 → 压测基线 → traceId → 虚拟线程对比实验）。
