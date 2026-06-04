# 多商户电商平台

多商户 B2C 电商平台：用户端（PC + H5）、商家后台、平台总控台。

## 技术栈

| 层级 | 选型 |
|------|------|
| 用户端 | Vue 3 + Vant（H5）+ Element Plus（PC） |
| 管理端 | Vue 3 + Element Plus |
| 后端 | Spring Boot 2.7 + MyBatis-Plus |
| 数据 | MySQL 8.0 + Redis |
| 部署 | Nginx + Docker + 阿里云 ECS |

## 仓库结构

```
├── backend/          # Spring Boot API（Phase 1 起实现）
├── frontend/
│   ├── user-app/     # 用户端
│   ├── merchant-admin/
│   └── platform-admin/
├── deploy/           # Docker、Nginx 配置
├── docs/             # PRD、数据库、决策记录
└── docker-compose.yml
```

## 快速开始（本地基建）

**前置**：Docker Desktop、Git

```bash
# 启动 MySQL + Redis
docker compose up -d

# 初始化数据库（首次）
docker compose exec mysql mysql -uroot -pmarket_root_pass market < backend/sql/V1__init_schema.sql
```

默认连接（见 `deploy/.env.example`）：

- MySQL：`localhost:3306`，库 `market`
- Redis：`localhost:6379`

## 分支策略

| 分支 | 用途 |
|------|------|
| `main` | 生产就绪代码 |
| `develop` | 集成开发 |
| `feature/*` | 功能分支，合并到 develop |

详见 [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md)。

## 文档

- [PRD v0.1](docs/PRD-v0.1.md)
- [数据库 ER 与 DDL](docs/database/ER.md)
- [Phase 0 检查清单](docs/phase0/CHECKLIST.md)

## 当前阶段

**Phase 0 — 启动与基建**（目标：2026-06-12）

Linear：[多商户电商平台](https://linear.app/code2michael/project/多商户电商平台-a4beddaf556b)
