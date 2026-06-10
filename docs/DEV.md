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

## 14. Epic 2.3 联调流程（商家数据看板）

基于本店已支付订单（待发货/已发货/已完成）聚合销售额与订单趋势。「下单用户」为去重买家数；独立访客埋点留待 V2。

| 步骤 | 操作 |
|------|------|
| 1 | 使用已审核通过的商家账号登录商家后台 http://localhost:5184 |
| 2 | 侧栏进入 **数据看板** |
| 3 | 查看概览卡片：今日/累计销售额、有效订单、待发货、近7日下单用户 |
| 4 | 切换 **近7天 / 近30天** 查看销售、订单量、下单用户趋势图 |

### 看板 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/merchant/stats/overview` | 概览指标（需商家登录） |
| GET | `/api/merchant/stats/trend?days=7` | 日趋势，`days` 支持 7–90 |

前端图表组件：`frontend/merchant-admin/src/components/BaseChart.vue`（ECharts 封装，总控台可复用）。

## 15. Epic 2.4 联调流程（平台交易统计）

全平台 GMV 基于已支付订单（待发货/已发货/已完成）的 `payAmount` 汇总；佣金来自 `commission_amount`。

| 步骤 | 操作 |
|------|------|
| 1 | 管理员登录总控台 http://localhost:5185 |
| 2 | 侧栏进入 **交易统计** |
| 3 | 查看今日/累计 GMV、订单、佣金、有交易店铺数 |
| 4 | 切换 **按日/周/月** 与 **近7/30/90天** 查看趋势图 |
| 5 | 查看商家 GMV 排行，可 **导出 CSV** |

### 统计 API（需管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/platform/stats/overview` | 平台概览 |
| GET | `/api/platform/stats/trend?days=30&granularity=day` | 趋势，`granularity`: day/week/month |
| GET | `/api/platform/stats/shops?days=30&limit=20` | 商家 GMV 排行 |
| GET | `/api/platform/stats/shops/export?days=30` | 导出 CSV |

## 16. Epic 3.1 联调流程（H5 移动端适配）

用户端 http://localhost:5183 已切换为 **底部 Tab 导航**（首页 / 分类 / 购物车 / 我的），PC 浏览器下内容区最大宽度 540px 居中。

| 步骤 | 操作 |
|------|------|
| 1 | 手机浏览器或 Chrome 设备模拟（iPhone/Android）打开用户端 |
| 2 | 首页：轮播、快捷入口、热门店铺预览 |
| 3 | **分类** Tab：搜索、类目 Tab、下拉刷新、骨架屏加载 |
| 4 | **购物车** Tab：侧滑删除、底部结算栏（适配 Tab 栏） |
| 5 | **我的** Tab：订单、热榜入口；订单页支持状态 Tab + 下拉刷新 |
| 6 | 商品详情：底部安全区购买栏 |

布局文件：`frontend/user-app/src/layouts/MainLayout.vue`、`src/styles/app.css`。

## 17. NL2-197 联调流程（商品主图 + 5 辅图本地上传）

商家端将「主图 URL」文本框替换为 **1 主图 + 5 辅图** 拖拽上传；用户端商品详情以轮播展示全部图片。

### 数据库迁移

在已有库上手动执行（Docker MySQL 示例）：

```bash
docker exec -i market-mysql mysql -umarket -pmarket_pass market < backend/sql/V3__product_gallery.sql
```

### 上传说明

| 项 | 值 |
|----|-----|
| 接口 | `POST /api/merchant/upload`（商家登录，字段 `file`） |
| 格式 | JPEG / PNG / WebP |
| 大小 | ≤ 2MB |
| 存储 | `backend/uploads/yyyy/MM/uuid.ext` |
| 访问 | `http://localhost:8082/uploads/...` |

前端 dev 已代理 `/uploads` → 8082，商家端与用户端均可直接显示相对路径图片。

### 联调步骤

| 步骤 | 操作 |
|------|------|
| 1 | 启动 Docker + 后端 + 商家端 http://localhost:5184 |
| 2 | 商家登录 → 发布/编辑商品 → 拖拽上传主图与辅图 → 保存 |
| 3 | 用户端 http://localhost:5183 打开该商品详情，确认轮播 |
| 4 | 多图时：每 **3 秒** 自动轮播，角标 `1/3` 同步；**点击图片** 全屏预览（可缩放、左右滑） |
| 5 | 真机：商家端 `npm run dev -- --host`，图片 URL 走 `http://<IP>:8082/uploads/...` |

### 用户端图库组件

- 文件：`frontend/user-app/src/components/ProductGallery.vue`
- 数据：`imageList` = `[mainImageUrl, ...galleryUrls]` 去重
- 轮播：`van-swipe`，多图时 `autoplay=3000`、`loop`、底部圆点
- 预览：`showImagePreview`，从当前索引打开

### 相关 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/merchant/upload` | 上传单张图片，返回 `{ url }` |
| POST/PUT | `/api/merchant/products` | `mainImageUrl` + `galleryUrls[]`（辅图 ≤5） |
| GET | `/api/products/{id}` | 详情含 `galleryUrls` |

## 18. Epic 3.2 联调流程（店铺关注 / 商品收藏 / 订单评价）

对应 Linear：NL2-140（Epic）、NL2-151（关注）、NL2-154（收藏）、NL2-153（评价）。表已在 `V1__init_schema.sql`：`user_shop_follow`、`user_favorite`、`product_review`。

### 相关 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/favorites` | 我的收藏列表 |
| GET | `/api/favorites/products/{id}/status` | 是否已收藏 |
| POST/DELETE | `/api/favorites/products/{id}` | 收藏 / 取消收藏 |
| GET | `/api/shops/following` | 我的关注店铺 |
| GET | `/api/shops/{id}/followed` | 是否已关注 |
| POST/DELETE | `/api/shops/{id}/follow` | 关注 / 取关 |
| POST | `/api/orders/{id}/review` | 已完成订单评价 `{ rating, content? }` |

订单详情 `OrderVO` 新增 `reviewed` 字段；评价后调用 `ShopRankService.refreshShop` 更新店铺热度。

### 联调步骤

| 步骤 | 操作 |
|------|------|
| 1 | 启动 Docker + 后端 8082 + 用户端 5183 |
| 2 | 用户登录 → 商品详情右上角星标收藏 → **我的 → 我的收藏** 可见 |
| 3 | **热门店铺榜** 点击「关注」→ **我的 → 我的关注** 可见 |
| 4 | 完成一笔订单并确认收货 → 订单详情「评价订单」→ 星级 + 文字提交 |
| 5 | 再次进入该订单，评价按钮消失（`reviewed=true`） |

### 前端入口

- `Profile.vue`：我的收藏、我的关注
- `ProductDetail.vue`：导航栏收藏星标
- `ShopRank.vue`：店铺行内关注按钮
- `OrderDetail.vue`：已完成未评价订单弹出评价面板

## 19. Epic 3.3 联调流程（站内信 / 订单通知）

对应 Linear：NL2-144（Epic）、NL2-149（站内信）。表已在 `V1__init_schema.sql`：`sys_message`。短信（NL2-150）、微信模板（NL2-148）留待外部账号对接。

### 相关 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/messages` | 我的消息列表 |
| GET | `/api/messages/unread-count` | 未读数量 `{ count }` |
| GET | `/api/messages/{id}` | 消息详情 |
| POST | `/api/messages/{id}/read` | 标为已读 |
| POST | `/api/messages/read-all` | 全部已读 |

### 自动通知时机

| 事件 | msg_type | 触发点 |
|------|----------|--------|
| 支付成功 | `order` | `OrderService.completePayment` |
| 商家发货 | `ship` | `OrderService.ship` |
| 确认收货 | `order` | `OrderService.confirmReceive` |
| 取消订单 | `order` | `OrderService.cancel` |

### 联调步骤

| 步骤 | 操作 |
|------|------|
| 1 | 重启后端（含 Message API） |
| 2 | 用户端登录 → 完成一笔订单支付 |
| 3 | **我的 → 我的消息** 出现「支付成功」，角标显示未读 |
| 4 | 商家发货 → 用户收到「商家已发货」 |
| 5 | 确认收货 → 收到「订单已完成」；点消息可跳转订单详情 |

### 前端入口

- `Profile.vue`：我的消息（未读角标）
- `Messages.vue`：列表、详情弹层、全部已读、查看订单

## 20. Epic 4.1 测试（NL2-145）

对应 Linear：NL2-145（Epic）、NL2-152（用例）、NL2-163（安全）、NL2-164（压测，可选）。

### 文档

| 文件 | 说明 |
|------|------|
| `docs/testing/E2E_TEST_CASES.md` | 三端 + API 功能/边界用例（可打印勾选） |
| `docs/testing/SECURITY_CHECKLIST.md` | 鉴权、IDOR、商家隔离、注入 |

### 自动化

```powershell
# 集成测试（自动跳过：Docker/MySQL/Redis 未就绪时）
cd d:\Armando\backend
.\mvnw.cmd test

# 对已运行后端快速冒烟（8082）
cd d:\Armando
.\scripts\api-smoke-test.ps1
```

集成测试类：`HealthApiIT`、`SecurityApiIT`、`IdorApiIT`（`src/test/java/com/market/api/`）。

CI：`.github/workflows/ci.yml` 在 push `develop` 时启动 MySQL/Redis 并执行 `mvn test`。

### 手工全链路建议顺序

1. 跑自动化 → 2. 按 `E2E_TEST_CASES.md` 冒烟 S-01~S-05 → 3. 章节 1~8 回归 → 4. `SECURITY_CHECKLIST.md` 商家隔离（SEC-30~32）手工项。

## 21. 目录说明

```
backend/          Spring Boot 2.7 + MyBatis-Plus
frontend/user-app       Vue3 + Vant
frontend/merchant-admin Vue3 + Element Plus
frontend/platform-admin Vue3 + Element Plus
```

## 22. MySQL 备份与恢复（NL2-157）

对应 Linear：NL2-157（MySQL 备份与恢复演练），Epic NL2-146。

### 快速命令

```powershell
cd d:\Armando
.\scripts\backup-mysql.ps1
.\scripts\restore-mysql.ps1 -BackupFile data\backups\mysql\market-<时间戳>.sql.gz
```

备份目录 `data/backups/mysql/` 已加入 `.gitignore`。

### 生产

Linux 脚本：`deploy/backup/backup-mysql.sh`、`restore-mysql.sh`；cron 示例见 `deploy/backup/crontab.example`。

完整说明与演练 checklist：`docs/deploy/MYSQL_BACKUP.md`。

## 23. 生产部署（NL2-158）

对应 Linear：NL2-158（Docker + Nginx + HTTPS），Epic NL2-146。

### 文件

| 文件 | 说明 |
|------|------|
| `docker-compose.prod.yml` | 生产栈：mysql、redis、api、nginx |
| `backend/Dockerfile` | API 镜像 |
| `deploy/Dockerfile.nginx` | 构建三端前端 + Nginx |
| `deploy/.env.production.example` | 环境变量模板 |

### ECS 一键部署

```bash
cp deploy/.env.production.example deploy/.env
# 配置域名、密码、JWT、SSL 证书
./deploy/scripts/deploy-prod.sh
```

详见 `docs/deploy/PRODUCTION_DEPLOY.md`。

## 24. 监控与日志（NL2-160）

对应 Linear：NL2-160，Epic NL2-146。

```bash
# 生产 ECS（需 docker-compose.prod 已运行）
./deploy/monitoring/check-health.sh
```

- 磁盘、容器、API health、Nginx 5xx 阈值告警
- 可选 Webhook：`deploy/monitoring/alert.env`
- 生产 API 日志：`/app/logs/market-api.log`（容器内）

详见 `docs/deploy/MONITORING.md`。

## 25. 上线 Checklist 与回滚（NL2-161）

对应 Linear：NL2-161，Epic NL2-146。

```bash
# 上线前（ECS）
./deploy/scripts/pre-launch-check.sh

# 回滚到上一稳定 commit
./deploy/scripts/rollback-release.sh --git-ref <SHA>
```

可打印勾选清单与回滚决策：`docs/deploy/LAUNCH_CHECKLIST.md`。
