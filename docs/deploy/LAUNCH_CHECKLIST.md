# 上线 Checklist 与回滚方案（NL2-161）

> **记录**：上线负责人 ______  计划日期 ______  生产版本 `develop` @ ______  
> **三端域名**：用户 ______  商家 ______  总控 ______

## 一、上线前（T-7 ~ T-1）

### 1.1 基础设施（Phase 0 外部项）

| # | 项 | 负责人 | 结果 |
|---|-----|--------|------|
| L-01 | ECS 4核8G 已就绪，安全组放行 80/443（[NL2-84](https://linear.app/code2michael/issue/NL2-84)） | 运维 | ☐ |
| L-02 | 域名 A 记录指向 ECS，三子域可解析（[NL2-83](https://linear.app/code2michael/issue/NL2-83)） | 运维 | ☐ |
| L-03 | TLS 证书有效 ≥30 天，已放入 `deploy/nginx/ssl/` | 运维 | ☐ |
| L-04 | `deploy/.env` 已配置强密码、`JWT_SECRET`≥32 字符 | 研发 | ☐ |
| L-05 | `PAYMENT_MOCK_ENABLED=false`（或书面记录继续 mock 的风险） | 产品 | ☐ |

### 1.2 备份与监控（Epic 4.2）

| # | 项 | 命令/文档 | 结果 |
|---|-----|-----------|------|
| L-06 | MySQL 备份脚本可跑 | `COMPOSE_FILE=docker-compose.prod.yml ./deploy/backup/backup-mysql.sh` | ☐ |
| L-07 | 备份恢复演练（预发/本地） | `docs/deploy/MYSQL_BACKUP.md` | ☐ |
| L-08 | 上传图片备份脚本可跑 | `./deploy/scripts/backup-uploads.sh` | ☐ |
| L-09 | 监控脚本 + cron | `./deploy/monitoring/check-health.sh` + crontab | ☐ |
| L-10 | 告警 Webhook 已测（可选） | `deploy/monitoring/alert.env` | ☐ |

### 1.3 代码与 CI

| # | 项 | 结果 |
|---|-----|------|
| L-11 | GitHub Actions CI 绿勾（`develop` 最新提交） | ☐ |
| L-12 | 预发环境 `deploy-prod.sh` 全流程成功 | ☐ |
| L-13 | 记录上线 commit SHA：________ | ☐ |
| L-14 | 记录上一稳定 commit SHA（回滚用）：________ | ☐ |

### 1.4 功能与安全抽检

| # | 项 | 文档 | 结果 |
|---|-----|------|------|
| L-15 | 冒烟 S-01~S-05 | `docs/testing/E2E_TEST_CASES.md` §0 | ☐ |
| L-16 | 下单→支付→发货→收货→消息 全链路 | E2E §3~5 | ☐ |
| L-17 | 鉴权 SEC-01~04、IDOR SEC-20 | `docs/testing/SECURITY_CHECKLIST.md` | ☐ |
| L-18 | 商家隔离 SEC-30~32（手工） | 安全清单 §4 | ☐ |

---

## 二、上线当日（T-0）

| 顺序 | 动作 | 命令/说明 | 完成 |
|------|------|-----------|------|
| 1 | **上线前备份** | DB + uploads 各一份，记录文件名 | ☐ |
| 2 | 跑自动化预检 | `./deploy/scripts/pre-launch-check.sh` | ☐ |
| 3 | 拉代码并部署 | `git pull && ./deploy/scripts/deploy-prod.sh` | ☐ |
| 4 | 健康检查 | `curl https://<USER_DOMAIN>/api/health` | ☐ |
| 5 | 三端浏览器冒烟 | S-02~S-05 生产域名 | ☐ |
| 6 | 启用/确认监控 cron | `deploy/monitoring/crontab.example` | ☐ |
| 7 | 公告/客服就绪（若需要） | 产品/运营 | ☐ |

**上线窗口建议**：低峰时段（如周二~周四 10:00–12:00），负责人全程在线 ≥2h。

---

## 三、上线后（T+1 ~ T+7）

| # | 项 | 结果 |
|---|-----|------|
| L-19 | 24h 内无 P0/P1 故障 | ☐ |
| L-20 | 监控日志无持续 5xx 告警 | ☐ |
| L-21 | 磁盘使用率 <85% | ☐ |
| L-22 | 首笔真实/mock 订单闭环复核 | ☐ |
| L-23 | 备份 cron 正常产出 | ☐ |
| L-24 | 复盘会议：记录问题与改进项 | ☐ |

---

## 四、回滚决策

满足 **任一** 条件即考虑回滚：

| 级别 | 条件 | 建议动作 |
|------|------|----------|
| P0 | 全站不可用 / 数据损坏风险 / 支付严重异常 | **立即回滚** + 停流量 |
| P1 | 核心链路（登录、下单、支付）大面积失败 | 30min 未恢复则回滚 |
| P2 | 非核心功能异常、可绕行 | 热修复，不回滚 |
| P3 | UI 小问题 | 排期修复 |

**回滚负责人**：研发 ______  运维 ______  决策人 ______

---

## 五、回滚步骤

### 5.1 仅应用回滚（推荐，数据库无破坏性变更）

```bash
cd /opt/ArmandoProject

# 1. 记录当前问题与 commit
git rev-parse HEAD

# 2. 回滚到上一稳定版本并重建
./deploy/scripts/rollback-release.sh --git-ref <上一稳定SHA>

# 3. 验证
curl -fsS https://<USER_DOMAIN>/api/health
./deploy/monitoring/check-health.sh
```

### 5.2 应用 + 数据库回滚（迁移失败或数据异常）

```bash
# 1. 使用上线前备份（T-0 步骤 1 的文件）
COMPOSE_FILE=docker-compose.prod.yml ./deploy/backup/restore-mysql.sh \
  data/backups/mysql/market-YYYYMMDD-HHMMSS.sql.gz

# 2. 再执行应用回滚
./deploy/scripts/rollback-release.sh --git-ref <上一稳定SHA>
```

> **注意**：DB 回滚会丢失回滚点之后的业务数据，需产品/运营确认。

### 5.3 仅重启服务（配置未变、进程僵死）

```bash
docker compose -f docker-compose.prod.yml --env-file deploy/.env restart api nginx
```

### 5.4 回滚后 checklist

| # | 项 | 结果 |
|---|-----|------|
| R-01 | `/api/health` 正常 | ☐ |
| R-02 | 冒烟 S-01~S-05 通过 | ☐ |
| R-03 | 监控恢复绿勾 | ☐ |
| R-04 | 事故记录写入复盘文档 | ☐ |

---

## 六、相关文档

| 文档 | 用途 |
|------|------|
| `docs/deploy/PRODUCTION_DEPLOY.md` | 部署 |
| `docs/deploy/MYSQL_BACKUP.md` | 备份恢复 |
| `docs/deploy/MONITORING.md` | 监控告警 |
| `docs/testing/E2E_TEST_CASES.md` | 功能验收 |
| `docs/testing/SECURITY_CHECKLIST.md` | 安全验收 |

Linear：NL2-161、Epic [NL2-146](https://linear.app/code2michael/issue/NL2-146)。
