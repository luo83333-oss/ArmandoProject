# 生产环境 Docker + Nginx 部署（NL2-158）

## 架构

```
Internet :443/:80
    └── nginx (TLS + 三端静态 + 反代)
            ├── /api/、/uploads/ → api:8082
            ├── shop.example.com      → 用户端
            ├── merchant.example.com  → 商家端
            └── admin.example.com     → 总控台
    api → mysql, redis
```

| 组件 | 文件 |
|------|------|
| 生产 Compose | `docker-compose.prod.yml` |
| API 镜像 | `backend/Dockerfile` + `application-prod.yml` |
| Nginx 镜像 | `deploy/Dockerfile.nginx`（构建三端 `dist`） |
| Nginx 配置 | `deploy/nginx/templates/market.conf.template` |
| 环境变量 | `deploy/.env.production.example` → `deploy/.env` |

## 前置条件

- [ ] ECS 已安装 Docker + Docker Compose（NL2-84）
- [ ] 域名 A 记录指向 ECS（NL2-83）
- [ ] `deploy/.env` 已填写强密码与 `JWT_SECRET`（≥32 字符）
- [ ] TLS 证书已放到 `deploy/nginx/ssl/fullchain.pem`、`privkey.pem`

## 上线前检查（NL2-161）

```bash
./deploy/scripts/pre-launch-check.sh
```

可打印 checklist：`docs/deploy/LAUNCH_CHECKLIST.md`（含回滚方案）。

## 首次部署（ECS Linux）

```bash
cd /opt/ArmandoProject
cp deploy/.env.production.example deploy/.env
# 编辑 deploy/.env：密码、JWT、三个域名

# Let's Encrypt 示例（需域名已解析）
# certbot certonly --standalone -d shop.example.com -d merchant.example.com -d admin.example.com
# cp /etc/letsencrypt/live/shop.example.com/fullchain.pem deploy/nginx/ssl/
# cp /etc/letsencrypt/live/shop.example.com/privkey.pem deploy/nginx/ssl/

chmod +x deploy/scripts/*.sh deploy/backup/*.sh
./deploy/scripts/deploy-prod.sh
```

`deploy-prod.sh` 会：构建镜像 → 启动栈 → 执行 V2/V3 迁移。

## 验证

```bash
curl -k https://shop.example.com/api/health
# {"code":0,"data":{"app":"ok","mysql":"ok","redis":"ok"}}

# 浏览器打开三端域名，总控台登录 13800000000 / admin123
```

## 备份（配合 NL2-157）

| 内容 | 命令 |
|------|------|
| MySQL | `COMPOSE_FILE=docker-compose.prod.yml ./deploy/backup/backup-mysql.sh` |
| 上传图片 | `./deploy/scripts/backup-uploads.sh` |

## 监控（NL2-160）

```bash
cp deploy/monitoring/alert.env.example deploy/monitoring/alert.env
chmod +x deploy/monitoring/check-health.sh
./deploy/monitoring/check-health.sh
# cron 见 deploy/monitoring/crontab.example
```

详见 `docs/deploy/MONITORING.md`。

## 更新发布

```bash
git pull
docker compose -f docker-compose.prod.yml --env-file deploy/.env up -d --build
```

## 本地预演（自签证书）

```bash
./deploy/scripts/generate-self-signed-ssl.sh
cp deploy/.env.production.example deploy/.env
# 域名可填 localhost，并在 hosts 中映射三个子域

docker compose -f docker-compose.prod.yml --env-file deploy/.env up -d --build
./deploy/scripts/init-prod-db.sh
```

> 自签证书仅用于预演；公网生产请用 Let's Encrypt 或云厂商证书。

## 环境变量说明

| 变量 | 说明 |
|------|------|
| `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | 生产 JWT 密钥（必改） |
| `USER_DOMAIN` | 用户端域名 |
| `MERCHANT_DOMAIN` | 商家端域名 |
| `ADMIN_DOMAIN` | 总控台域名 |
| `PAYMENT_MOCK_ENABLED` | 生产建议 `false` |

## 故障排查

| 现象 | 处理 |
|------|------|
| nginx 启动失败 | 检查 `deploy/nginx/ssl/` 证书是否存在 |
| api 不健康 | `docker logs market-api`；查 MySQL/Redis/JWT_SECRET |
| 502 on /api | `docker compose ... ps` 确认 api healthy |
| 前端白屏 | 确认域名与 `deploy/.env` 中一致；查 nginx 日志 |
| 图片 404 | 确认 `uploads_data` 卷有数据；`/uploads/` 反代正常 |

## Linear

NL2-158（本任务）、父 Epic NL2-146；依赖 NL2-83/84（域名与 ECS）。
