# MySQL 备份与恢复演练（NL2-157）

## 目标

| 指标 | 目标 |
|------|------|
| RPO（可接受数据丢失） | ≤ 24h（每日备份）；生产建议 cron 每日 03:15 |
| RTO（恢复耗时） | ≤ 30min（逻辑备份恢复 + 重启 API） |
| 保留策略 | 本地开发 7 天；生产 14 天（可环境变量调整） |

## 备份内容

- 数据库：`market`（业务全库逻辑备份）
- 方式：`mysqldump --single-transaction`（InnoDB 一致性，不停服）
- **不含**：`backend/uploads/` 商品图片（需单独 rsync/OSS 备份，见 NL2-158）

## 本地（Windows + Docker）

### 备份

```powershell
cd d:\Armando
.\scripts\backup-mysql.ps1
```

输出：`data/backups/mysql/market-YYYYMMDD-HHMMSS.sql.gz`

### 恢复演练

1. 先备份当前库（可选）
2. 恢复：

```powershell
.\scripts\restore-mysql.ps1 -BackupFile data\backups\mysql\market-20260609-120000.sql.gz
# 提示时输入 RESTORE 确认
```

3. 验证：

```powershell
Invoke-WebRequest http://localhost:8082/api/health -UseBasicParsing
.\scripts\api-smoke-test.ps1
```

### 演练 checklist

- [ ] `backup-mysql.ps1` 生成 `.sql.gz` 且大小 > 1KB
- [ ] 恢复后 `/api/health` 中 `mysql: ok`
- [ ] 登录总控台、查看订单/商品数据与恢复前一致
- [ ] 记录本次备份文件名与演练日期

## 生产（Linux ECS）

```bash
cd /opt/ArmandoProject
chmod +x deploy/backup/*.sh
./deploy/backup/backup-mysql.sh
```

定时任务：复制 `deploy/backup/crontab.example` 到服务器 `crontab -e`。

恢复：

```bash
./deploy/backup/restore-mysql.sh data/backups/mysql/market-20260609-031500.sql.gz
```

## 环境变量

与 `docker-compose.yml` / `deploy/.env` 一致：

| 变量 | 默认 |
|------|------|
| `MYSQL_USER` | market |
| `MYSQL_PASSWORD` | market_pass |
| `MYSQL_ROOT_PASSWORD` | market_root_pass |
| `MYSQL_DATABASE` | market |
| `RETENTION_DAYS` | 7（PS1）/ 14（sh） |

## 故障排查

| 现象 | 处理 |
|------|------|
| 容器未运行 | `docker compose up -d mysql` |
| 备份文件过小 | 检查账号密码、库名、容器日志 `docker logs market-mysql` |
| 恢复后 500 | 重启后端；确认 V2/V3 迁移已在备份前执行 |
| 生产磁盘满 | 调小 `RETENTION_DAYS` 或挂载独立备份盘 |

## 相关

- `docker-compose.yml` — MySQL 服务定义
- `docs/DEV.md` §22 — 联调入口
- Linear：NL2-157、父 Epic NL2-146
