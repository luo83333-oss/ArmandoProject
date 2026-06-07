# 本地开发联调指南（Phase 1 / Epic 1.2）

## 前置

- Docker Desktop 运行中：`docker compose up -d`
- **JDK 17**（你已安装 `Microsoft.OpenJDK.17`，装后需**新开终端**）
- **Maven**：项目自带 `mvnw.cmd`，**无需**全局安装 Maven
- **Node.js 18+**（你本机已有 v22 即可）

## 1. 启动基础设施

```powershell
cd d:\Armando
docker compose up -d
docker compose ps
```

MySQL：`localhost:3306` / 库 `market` / 用户 `market` / 密码 `market_pass`

## 2. 配置 JAVA_HOME（JDK 装好后做一次）

**设置 → 系统 → 关于 → 高级系统设置 → 环境变量**：

- 新建用户变量 `JAVA_HOME` = `C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot`
- 编辑 `Path`，加入 `%JAVA_HOME%\bin`

或每次开发前在 PowerShell 执行：

```powershell
cd d:\Armando\backend
. .\scripts\dev-env.ps1
```

**新开终端**后执行 `java -version` 应有输出。

## 3. 启动后端 API（端口 8082）

```powershell
cd d:\Armando\backend
.\mvnw.cmd spring-boot:run
```

> 首次运行会自动下载 Maven 3.9.9 到用户目录，需能访问 `repo.maven.apache.org`（网络不稳可多试几次）。

健康检查：http://localhost:8082/api/health

期望响应：

```json
{
  "code": 0,
  "message": "success",
  "data": { "app": "ok", "mysql": "ok", "redis": "ok" }
}
```

## 4. 启动前端（各开一终端）

```powershell
cd d:\Armando\frontend\user-app
npm install
npm run dev
```

| 端 | 目录 | 地址 |
|----|------|------|
| 用户端 H5 | `frontend/user-app` | http://localhost:5183 |
| 商家后台 | `frontend/merchant-admin` | http://localhost:5184 |
| 平台总控台 | `frontend/platform-admin` | http://localhost:5185 |

> Armando 使用 **8082 / 5183–5185** 专用端口，避免与 DeepSeek 等项目（5173、8081 等）冲突。

```powershell
cd d:\Armando\frontend\merchant-admin
npm install && npm run dev

cd d:\Armando\frontend\platform-admin
npm install && npm run dev
```

各端首页有「检测 API」按钮，经 Vite 代理访问 `/api/health`。

## 5. Epic 1.2 数据库迁移（已有 Docker 库时执行一次）

首次 `docker compose up` 只会跑 `V1__init_schema.sql`。若库已存在，需手动执行 V2：

```powershell
Get-Content d:\Armando\backend\sql\V2__add_user_role.sql | docker compose exec -T mysql mysql -umarket -pmarket_pass market
```

## 6. Epic 1.2 联调流程

| 步骤 | 操作 |
|------|------|
| 1 | 用户端 http://localhost:5183 注册（验证码固定 `123456`） |
| 2 | 商家后台 http://localhost:5184 用同一账号登录 → 提交入驻申请 |
| 3 | 平台总控台 http://localhost:5185 登录 `13800000000` / `admin123`（后端首次启动自动创建） |
| 4 | 在总控台「通过」申请 → 商家后台可看到店铺名称 |

### 主要 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/send-code` | 发送验证码（开发占位） |
| POST | `/api/auth/register` | 手机号注册 |
| POST | `/api/auth/login` | 登录，返回 JWT |
| POST | `/api/auth/wechat` | 微信登录 Mock |
| GET | `/api/auth/me` | 当前用户（需 Bearer Token） |
| POST | `/api/merchant/apply` | 商家入驻申请 |
| GET | `/api/merchant/mine` | 我的入驻状态 |
| GET | `/api/platform/merchants` | 平台审核列表（管理员） |
| POST | `/api/platform/merchants/{id}/audit` | 通过/驳回/冻结 |

审核动作 `action`：`1` 通过、`2` 驳回、`3` 冻结。

## 7. Epic 1.3 联调流程（商品管理）

| 步骤 | 操作 |
|------|------|
| 1 | 确保已完成 Epic 1.2 商家入驻审核 |
| 2 | 商家后台 → **商品管理** → **发布商品**（选类目、填 SKU 价格库存） |
| 3 | 在商品列表点 **上架** |
| 4 | 用户端首页 → **逛商品** → 搜索/分类筛选 → 查看详情 |

后端首次启动会自动写入商品类目种子（数码/服装/食品）。`product.violation_flag` 字段已预留供 Phase 2 违规审核。

### 商品 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/categories` | 类目树（公开） |
| GET | `/api/products` | 用户端商品列表（`keyword`、`categoryId`、分页） |
| GET | `/api/products/{id}` | 商品详情（仅上架） |
| GET | `/api/merchant/products` | 商家商品列表（需登录+已入驻） |
| POST | `/api/merchant/products` | 创建商品 |
| PUT | `/api/merchant/products/{id}` | 更新商品 |
| PATCH | `/api/merchant/products/{id}/shelf` | 上下架 `{ "shelfStatus": 1 }` |
| DELETE | `/api/merchant/products/{id}` | 删除商品 |

## 8. Epic 1.4 联调流程（购物车与下单）

| 步骤 | 操作 |
|------|------|
| 1 | 用户端登录 → 商品详情 → **加入购物车** 或 **立即购买** |
| 2 | **购物车** 勾选商品 → **去结算** |
| 3 | 填写收货地址 → **提交订单** |
| 4 | 订单详情页 → **模拟支付** → 状态变为「已付款」 |

### 购物车/订单 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/cart` | 购物车列表 |
| POST | `/api/cart` | 加入 `{ skuId, quantity }` |
| PUT | `/api/cart/{id}` | 更新数量/勾选 |
| DELETE | `/api/cart/{id}` | 删除 |
| POST | `/api/orders/checkout` | 下单（已勾选商品） |
| GET | `/api/orders` | 我的订单 |
| GET | `/api/orders/{id}` | 订单详情 |
| POST | `/api/orders/{id}/pay` | 模拟支付 |

## 9. Epic 1.5 联调流程（订单状态机）

订单状态：`10` 待付款 → `20` 待发货 → `30` 已发货 → `40` 已完成；待付款可取消为 `50`。

| 步骤 | 操作 |
|------|------|
| 1 | 用户端完成 Epic 1.4 下单并 **模拟支付**（状态变为「待发货」） |
| 2 | 商家后台 → **订单管理** → 筛选「待发货」→ **发货** 填写物流单号 |
| 3 | 用户端 **我的订单** → 订单详情 → **确认收货**（状态变为「已完成」） |
| 4 | 已完成订单详情显示「申请售后（V2 开放）」占位按钮 |

### 订单状态 API（Epic 1.5 新增）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/merchant/orders` | 商家订单列表（`status` 可选） |
| GET | `/api/merchant/orders/{id}` | 商家订单详情 |
| POST | `/api/merchant/orders/{id}/ship` | 发货 `{ logisticsNo }` |
| POST | `/api/orders/{id}/confirm` | 用户确认收货 |
| POST | `/api/orders/{id}/cancel` | 用户取消（仅待付款） |

每次状态变更会写入 `order_status_log` 表。

## 10. 常见问题

| 问题 | 处理 |
|------|------|
| mysql/redis error | `docker compose ps` 确认 healthy |
| 8082 端口占用 | 修改 `application.yml` 的 `server.port` |
| npm 慢 | `npm config set registry https://registry.npmmirror.com` |

## 11. Epic 1.6 联调流程（总控台监管）

| 步骤 | 操作 |
|------|------|
| 1 | 平台总控台 http://localhost:5185 登录 `13800000000` / `admin123` |
| 2 | **商家审核**：审核/驳回/冻结入驻申请（Epic 1.2 已有） |
| 3 | **用户管理**：查看注册用户，禁用/启用（管理员账号不可操作） |
| 4 | **订单监管**：查看全平台订单，按状态筛选，点详情查看商品明细 |

### 平台监管 API（Epic 1.6 新增）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/platform/users` | 用户列表 |
| PATCH | `/api/platform/users/{id}/status` | 禁用/启用 `{ "status": 0\|1 }` |
| GET | `/api/platform/orders` | 全平台订单（`status`、`shopId` 可选） |
| GET | `/api/platform/orders/{id}` | 订单详情 |

### MVP 端到端验收清单

入驻 → 发品上架 → 用户下单支付 → 商家发货 → 用户确认收货 → 总控台可查订单与用户。

## 12. Epic 2.1 联调流程（支付渠道）

> 真实微信/支付宝需商户号；开发环境使用 **mock** 与 **wechat/alipay 沙箱** 流程。

| 步骤 | 操作 |
|------|------|
| 1 | 用户端下单后进入订单详情 |
| 2 | 选择支付方式：模拟支付 / 微信 / 支付宝 |
| 3 | **模拟支付**：点击后立即成功，写入 `payment_record` |
| 4 | **微信/支付宝沙箱**：点击「唤起支付」→ 再点「完成沙箱支付」模拟第三方回调 |
| 5 | 支付成功后订单变为「待发货」，与 Epic 1.5 流程衔接 |

配置见 `application.yml` → `market.payment`（`mock-enabled` / `wechat-enabled` / `alipay-enabled`）。

### 支付 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/payment/channels` | 可用支付渠道 |
| POST | `/api/orders/{id}/pay` | 发起支付 `{ "channel": "mock\|wechat\|alipay" }` |
| POST | `/api/payment/sandbox/complete` | 沙箱完成支付（需登录） |
| POST | `/api/payment/callback/{channel}` | 第三方回调（幂等，无需登录） |

## 13. Epic 2.2 联调流程（店铺权重排行）

权重公式：`权重分 = 有效订单数×销量权重 + 评价数×评价权重 − 违规商品数×违规扣分`

有效订单 = 已支付及之后状态（待发货/已发货/已完成）。排行数据写入 Redis ZSET `market:shop:rank`，并快照到 `shop_rank_snapshot`。

| 步骤 | 操作 |
|------|------|
| 1 | 确保已有商家订单（完成几笔支付/收货更佳） |
| 2 | 总控台 http://localhost:5185 → **店铺排行** → **重新计算排行** |
| 3 | 用户端首页 → **热门店铺榜** 查看排名 |
| 4 | 可调整权重系数后再次重算，观察排名变化 |

支付成功、确认收货后会自动刷新该店铺权重（增量更新 Redis）。

### 排行 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/shops/rank` | 用户端热门榜（公开） |
| GET | `/api/platform/rank/config` | 权重配置（管理员） |
| PUT | `/api/platform/rank/config` | 更新权重系数 |
| POST | `/api/platform/rank/recalculate` | 全量重算排行 |
| GET | `/api/platform/rank` | 管理端查看排行榜 |

## 14. 目录说明

```
backend/          Spring Boot 2.7 + MyBatis-Plus
frontend/user-app       Vue3 + Vant
frontend/merchant-admin Vue3 + Element Plus
frontend/platform-admin Vue3 + Element Plus
```
