# 数据库 ER 说明（V1 草案）

## ER 图

```mermaid
erDiagram
  sys_user ||--o| merchant_account : "1:0..1"
  merchant_account ||--|| shop : "1:1"
  shop ||--o{ product : "1:N"
  product ||--o{ product_sku : "1:N"
  sys_user ||--o{ cart_item : "1:N"
  sys_user ||--o{ user_order : "1:N"
  shop ||--o{ user_order : "1:N"
  user_order ||--|{ order_item : "1:N"
  user_order ||--o| payment_record : "1:0..1"
  user_order ||--o{ order_status_log : "1:N"
  shop ||--o{ shop_rank_snapshot : "1:N"

  sys_user {
    bigint id PK
    varchar phone UK
    varchar password_hash
    varchar nickname
    tinyint status
    datetime created_at
  }

  merchant_account {
    bigint id PK
    bigint user_id FK
    varchar company_name
    varchar license_no
    tinyint audit_status
    datetime created_at
  }

  shop {
    bigint id PK
    bigint merchant_id FK
    varchar name
    varchar logo_url
    tinyint status
    decimal weight_score
  }

  product {
    bigint id PK
    bigint shop_id FK
    bigint category_id FK
    varchar title
    tinyint shelf_status
    tinyint violation_flag
  }

  product_sku {
    bigint id PK
    bigint product_id FK
    varchar spec_json
    decimal price
    int stock
  }

  user_order {
    bigint id PK
    varchar order_no UK
    bigint user_id FK
    bigint shop_id FK
    decimal total_amount
    decimal commission_amount
    tinyint status
    varchar address_json
  }

  order_item {
    bigint id PK
    bigint order_id FK
    bigint sku_id FK
    int quantity
    decimal unit_price
  }

  payment_record {
    bigint id PK
    bigint order_id FK
    varchar channel
    varchar trade_no
    tinyint pay_status
  }
```

## 表清单

| 表名 | 说明 | Phase |
|------|------|-------|
| `sys_user` | C 端用户 | 1 |
| `merchant_account` | 商家主体与审核 | 1 |
| `shop` | 店铺 | 1 |
| `product_category` | 商品类目树 | 1 |
| `product` | SPU | 1 |
| `product_sku` | SKU 库存价格 | 1 |
| `cart_item` | 购物车 | 1 |
| `user_order` | 订单主表 | 1 |
| `order_item` | 订单明细 | 1 |
| `order_status_log` | 状态流水 | 1 |
| `payment_record` | 支付记录 | 1（模拟）/ 2（真实） |
| `product_review` | 商品评价 | 3 |
| `user_favorite` | 收藏 | 3 |
| `user_shop_follow` | 店铺关注 | 3 |
| `shop_rank_snapshot` | 排行快照 | 2 |
| `platform_config` | 权重系数等 | 2 |
| `sys_message` | 站内信 | 3 |

DDL 见 [backend/sql/V1__init_schema.sql](../../backend/sql/V1__init_schema.sql)。

## 订单状态机

```
PENDING_PAY(10) → PAID(20) → SHIPPED(30) → COMPLETED(40)
                 ↘ CANCELLED(50)
```

Phase 1 模拟支付：创建订单后可直接调用「模拟支付成功」→ `PAID`。
