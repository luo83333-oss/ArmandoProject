# 监控与日志（NL2-160）

## 覆盖范围

| 项 | 方式 |
|----|------|
| 磁盘 | `check-health.sh` 检查根分区 ≥85% 告警 |
| 5xx | 解析 Nginx 标准输出访问日志，1h 内 ≥20 次告警 |
| 服务存活 | docker compose 检查 mysql/redis/api/nginx |
| API | 容器内 `curl /api/health` |
| 应用日志 | 生产 `logback-spring.xml` → stdout + `/app/logs/market-api.log` |
| Nginx 日志 | stdout/stderr（`docker compose logs nginx`） |

## 快速启用（ECS）

```bash
cd /opt/ArmandoProject
cp deploy/monitoring/alert.env.example deploy/monitoring/alert.env
# 可选：填写 ALERT_WEBHOOK_URL（钉钉/企微/Slack）

chmod +x deploy/monitoring/check-health.sh
./deploy/monitoring/check-health.sh

# 定时每 5 分钟
crontab -e   # 参考 deploy/monitoring/crontab.example
```

## 手动查看日志

```bash
# API（最近 200 行）
docker compose -f docker-compose.prod.yml --env-file deploy/.env logs --tail=200 api

# Nginx 5xx 抽样
docker compose -f docker-compose.prod.yml --env-file deploy/.env logs --since 1h nginx \
  | grep -E 'HTTP/[0-9.]+" 5[0-9]{2}'

# 容器内滚动日志文件
docker compose -f docker-compose.prod.yml --env-file deploy/.env exec api \
  tail -n 100 /app/logs/market-api.log
```

## 环境变量

| 变量 | 默认 | 说明 |
|------|------|------|
| `DISK_THRESHOLD` | 85 | 磁盘使用率 % |
| `HTTP5XX_THRESHOLD` | 20 | 时间窗内 5xx 次数 |
| `LOG_WINDOW` | 1h | Nginx 日志统计窗口 |
| `ALERT_WEBHOOK_URL` | 空 | 告警 Webhook |

## 告警 Webhook 示例

钉钉机器人：

```bash
ALERT_WEBHOOK_URL=https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
```

脚本发送 JSON：`{"text":"[market-monitor] ... ALERT: ..."}`。若机器人要求签名，请在网关侧配置或扩展脚本。

## 本地开发

开发环境仍用控制台日志（`application.yml` `logging.level.com.market: debug`）。  
监控脚本面向 `docker-compose.prod.yml`，本地可不启用 cron。

## 相关

- `deploy/nginx/templates/market.conf.template` — `market` 日志格式含 `$status`
- Linear：NL2-160、Epic NL2-146
