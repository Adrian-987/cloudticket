# JMeter 压测脚本

## 脚本清单

| 文件 | 场景 | 说明 |
|---|---|---|
| `cloudticket-detail.jmx` | 详情 `GET /user/shows/1001` | 200 线程 × 10 循环 = 2000 请求 |

## 怎么跑（命令行，官方推荐）

```bash
cd D:/download/apache-jmeter-5.6.3/bin
java -jar ApacheJMeter.jar -n -t "D:/code files/个人第一个项目/docs/jmeter/cloudticket-detail.jmx" -l result.jtl
# -n 非 GUI；-t 脚本；-l 结果文件（CSV）
```

## 用 GUI 打开（只做调试）

```
双击 jmeter.bat → 文件 → 打开 → 选中 .jmx
注意：GUI 模式压不出真实 QPS（UI 渲染限制 ~1000），正式压测必须用命令行
```

## 结果解读

命令行输出的 summary 行：

```
summary = 2000 in 00:00:01 = 1623.4/s Avg: 12 Min: 1 Max: 96 Err: 0 (0.00%)
                              ↑ QPS      ↑ 平均  ↑ 最小 ↑ 最大 ↑ 错误率
```

> 历史数据见 `docs/压测记录.md`（A/B/C/D5 各组）。
