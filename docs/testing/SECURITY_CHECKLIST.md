# Epic 4.1 安全测试清单（NL2-163）

> 重点：鉴权、越权（IDOR）、商家数据隔离、注入与异常输入。  
> 自动化部分见 `backend/src/test/java/com/market/api/SecurityApiIT.java`、`IdorApiIT.java`。

## 1. 鉴权（Authentication）

| ID | 测试 | 操作 | 预期 | 自动 | 结果 |
|----|------|------|------|------|------|
| SEC-01 | 无 Token | `GET /api/orders` 无 Header | `code=401` | ✅ | ☐ |
| SEC-02 | 无效 Token | `Authorization: Bearer invalid` | `code=401` | ✅ | ☐ |
| SEC-03 | 过期 Token | 使用过期 JWT（若有） | `code=401` | 手工 | ☐ |
| SEC-04 | 公开接口 | `GET /api/products` 无 Token | `code=0` | ✅ | ☐ |

## 2. 授权（Authorization / 角色）

| ID | 测试 | 操作 | 预期 | 自动 | 结果 |
|----|------|------|------|------|------|
| SEC-10 | 买家访问平台 | 买家 token → `GET /api/platform/orders` | `code=403` | ✅ | ☐ |
| SEC-11 | 买家访问商家发品 | 买家 token → `POST /api/merchant/products` | 403 或业务拒绝 | 手工 | ☐ |
| SEC-12 | 管理员 | admin token → `GET /api/platform/orders` | `code=0` | 手工 | ☐ |

## 3. IDOR（用户水平越权）

| ID | 测试 | 操作 | 预期 | 自动 | 结果 |
|----|------|------|------|------|------|
| SEC-20 | 他人订单 | 用户 B token → `GET /api/orders/{A的订单ID}` | `code=404`（不泄露存在性） | ✅ | ☐ |
| SEC-21 | 他人消息 | 用户 B → `GET /api/messages/{A的消息ID}` | `code=404` | 手工 | ☐ |
| SEC-22 | 他人收藏 | 用户 B 无法删除 A 的收藏 | 404/无影响 | 手工 | ☐ |

## 4. 商家隔离

| ID | 测试 | 操作 | 预期 | 结果 |
|----|------|------|------|------|
| SEC-30 | 订单隔离 | 商家 A → `GET /api/merchant/orders/{B店订单ID}` | 404 或拒绝 | ☐ |
| SEC-31 | 商品隔离 | 商家 A 编辑 B 店商品 ID | 拒绝 | ☐ |
| SEC-32 | 发货隔离 | 商家 A 对 B 店订单发货 | 拒绝 | ☐ |

## 5. 注入与异常输入

| ID | 测试 | 操作 | 预期 | 自动 | 结果 |
|----|------|------|------|------|------|
| SEC-40 | SQL 注入关键词 | `GET /api/products?keyword=' OR 1=1--` | 不 500，无异常泄露 | ✅ | ☐ |
| SEC-41 | 路径遍历 | 商品 ID `../../../etc/passwd` | 400/404，不 500 | 手工 | ☐ |
| SEC-42 | 超长输入 | 评价 content > 1024 字 | 校验失败 | 手工 | ☐ |
| SEC-43 | 负数/零 | 购物车 quantity=0 或 -1 | 校验失败 | 手工 | ☐ |

## 6. 上传安全（NL2-197）

| ID | 测试 | 操作 | 预期 | 结果 |
|----|------|------|------|------|
| SEC-50 | 非图片 | 上传 `.exe` 伪装 | 拒绝 | ☐ |
| SEC-51 | 超大文件 | > 2MB 图片 | 拒绝 | ☐ |
| SEC-52 | 未登录上传 | 无 token 调 `/api/merchant/upload` | 401 | ☐ |

## 执行说明

```powershell
# 自动化（需 Docker + 后端依赖由 Spring Boot 测试自启）
cd d:\Armando\backend
.\mvnw.cmd test

# 对已运行实例快速冒烟
cd d:\Armando
.\scripts\api-smoke-test.ps1
```
