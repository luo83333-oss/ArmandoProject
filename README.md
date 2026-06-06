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

## 快速开始

**Phase 0**：Docker 基建

```powershell
docker compose up -d
```

**Phase 1**：API + 三端开发 — 详见 [docs/DEV.md](docs/DEV.md)

```powershell
cd backend && .\mvnw.cmd spring-boot:run
cd frontend/user-app && npm install && npm run dev
```

| 服务 | 地址 |
|------|------|
| API | http://localhost:8082/api/health |
| 用户端 | http://localhost:5183 |
| 商家端 | http://localhost:5184 |
| 总控台 | http://localhost:5185 |
| MySQL | localhost:3306 / market |
| Redis | localhost:6379 |

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

**Phase 1 — MVP 第 3 周（Epic 1.3）**：商品管理

- 商家后台：商品 CRUD、SKU/库存、上下架
- 用户端：商品列表/详情、关键词搜索、类目筛选
- 后端：类目树、违规字段预留（`violation_flag`）

联调步骤见 [docs/DEV.md](docs/DEV.md) 第 7 节。

Linear：[多商户电商平台](https://linear.app/code2michael/project/多商户电商平台-a4beddaf556b)
