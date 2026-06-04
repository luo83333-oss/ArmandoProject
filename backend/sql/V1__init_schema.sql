-- 多商户电商平台 V1 初始化 DDL
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE market;

-- ========== 用户与商家 ==========
CREATE TABLE sys_user (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    phone           VARCHAR(20) NOT NULL COMMENT '手机号',
    password_hash   VARCHAR(128) NOT NULL,
    nickname        VARCHAR(64) DEFAULT NULL,
    avatar_url      VARCHAR(512) DEFAULT NULL,
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB COMMENT='C端用户';

CREATE TABLE merchant_account (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL COMMENT '登录账号',
    company_name    VARCHAR(128) NOT NULL,
    contact_name    VARCHAR(64) NOT NULL,
    contact_phone   VARCHAR(20) NOT NULL,
    license_no      VARCHAR(64) DEFAULT NULL COMMENT '营业执照号',
    license_file_url VARCHAR(512) DEFAULT NULL,
    audit_status    TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回 3冻结',
    audit_remark    VARCHAR(512) DEFAULT NULL,
    audited_at      DATETIME DEFAULT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_audit (audit_status)
) ENGINE=InnoDB COMMENT='商家主体';

CREATE TABLE shop (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    merchant_id     BIGINT UNSIGNED NOT NULL,
    name            VARCHAR(128) NOT NULL,
    logo_url        VARCHAR(512) DEFAULT NULL,
    description     VARCHAR(1024) DEFAULT NULL,
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '1营业 0关闭',
    weight_score    DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '权重分',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_merchant (merchant_id)
) ENGINE=InnoDB COMMENT='店铺';

-- ========== 商品 ==========
CREATE TABLE product_category (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    parent_id       BIGINT UNSIGNED NOT NULL DEFAULT 0,
    name            VARCHAR(64) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    status          TINYINT NOT NULL DEFAULT 1,
    KEY idx_parent (parent_id)
) ENGINE=InnoDB COMMENT='商品类目';

CREATE TABLE product (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    shop_id         BIGINT UNSIGNED NOT NULL,
    category_id     BIGINT UNSIGNED NOT NULL,
    title           VARCHAR(256) NOT NULL,
    main_image_url  VARCHAR(512) DEFAULT NULL,
    detail_html     MEDIUMTEXT,
    shelf_status    TINYINT NOT NULL DEFAULT 0 COMMENT '0下架 1上架',
    violation_flag  TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1违规待审',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_shop_shelf (shop_id, shelf_status),
    FULLTEXT KEY ft_title (title)
) ENGINE=InnoDB COMMENT='商品SPU';

CREATE TABLE product_sku (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT UNSIGNED NOT NULL,
    spec_json       JSON NOT NULL COMMENT '规格键值',
    price           DECIMAL(12,2) NOT NULL,
    stock           INT NOT NULL DEFAULT 0,
    sku_code        VARCHAR(64) DEFAULT NULL,
    status          TINYINT NOT NULL DEFAULT 1,
    KEY idx_product (product_id)
) ENGINE=InnoDB COMMENT='商品SKU';

-- ========== 购物车与订单 ==========
CREATE TABLE cart_item (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    sku_id          BIGINT UNSIGNED NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    selected        TINYINT NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_sku (user_id, sku_id)
) ENGINE=InnoDB COMMENT='购物车';

CREATE TABLE user_order (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(32) NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    shop_id         BIGINT UNSIGNED NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    pay_amount      DECIMAL(12,2) NOT NULL,
    commission_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0500 COMMENT '抽佣比例快照',
    commission_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status          TINYINT NOT NULL COMMENT '10待付 20已付 30已发 40完成 50取消',
    address_json    JSON NOT NULL,
    logistics_no    VARCHAR(64) DEFAULT NULL,
    paid_at         DATETIME DEFAULT NULL,
    shipped_at      DATETIME DEFAULT NULL,
    completed_at    DATETIME DEFAULT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_shop (shop_id),
    KEY idx_status (status)
) ENGINE=InnoDB COMMENT='订单';

CREATE TABLE order_item (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT UNSIGNED NOT NULL,
    product_id      BIGINT UNSIGNED NOT NULL,
    sku_id          BIGINT UNSIGNED NOT NULL,
    product_title   VARCHAR(256) NOT NULL,
    spec_json       JSON NOT NULL,
    quantity        INT NOT NULL,
    unit_price      DECIMAL(12,2) NOT NULL,
    KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='订单明细';

CREATE TABLE order_status_log (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT UNSIGNED NOT NULL,
    from_status     TINYINT DEFAULT NULL,
    to_status       TINYINT NOT NULL,
    operator_type   VARCHAR(16) NOT NULL COMMENT 'user/merchant/system/admin',
    operator_id     BIGINT UNSIGNED DEFAULT NULL,
    remark          VARCHAR(256) DEFAULT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='订单状态流水';

CREATE TABLE payment_record (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT UNSIGNED NOT NULL,
    channel         VARCHAR(16) NOT NULL COMMENT 'mock/wechat/alipay',
    trade_no        VARCHAR(64) DEFAULT NULL,
    pay_amount      DECIMAL(12,2) NOT NULL,
    pay_status      TINYINT NOT NULL DEFAULT 0 COMMENT '0待付 1成功 2失败',
    callback_raw    JSON DEFAULT NULL,
    paid_at         DATETIME DEFAULT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_channel (order_id, channel)
) ENGINE=InnoDB COMMENT='支付记录';

-- ========== Phase 2+ 预留 ==========
CREATE TABLE platform_config (
    config_key      VARCHAR(64) NOT NULL PRIMARY KEY,
    config_value    VARCHAR(512) NOT NULL,
    remark          VARCHAR(256) DEFAULT NULL,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='平台配置';

INSERT INTO platform_config (config_key, config_value, remark) VALUES
('commission.rate', '0.05', '默认抽佣比例 5%'),
('rank.sale_weight', '1.0', '销量权重系数'),
('rank.review_weight', '0.5', '评价权重系数'),
('rank.violation_penalty', '10', '违规扣分');

CREATE TABLE shop_rank_snapshot (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    shop_id         BIGINT UNSIGNED NOT NULL,
    rank_score      DECIMAL(12,4) NOT NULL,
    rank_position   INT NOT NULL,
    snapshot_date   DATE NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_date_pos (snapshot_date, rank_position)
) ENGINE=InnoDB COMMENT='店铺排行快照';

CREATE TABLE product_review (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    product_id      BIGINT UNSIGNED NOT NULL,
    shop_id         BIGINT UNSIGNED NOT NULL,
    rating          TINYINT NOT NULL COMMENT '1-5',
    content         VARCHAR(1024) DEFAULT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order (order_id)
) ENGINE=InnoDB COMMENT='评价';

CREATE TABLE user_favorite (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    product_id      BIGINT UNSIGNED NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB COMMENT='商品收藏';

CREATE TABLE user_shop_follow (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    shop_id         BIGINT UNSIGNED NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_shop (user_id, shop_id)
) ENGINE=InnoDB COMMENT='店铺关注';

CREATE TABLE sys_message (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    title           VARCHAR(128) NOT NULL,
    content         VARCHAR(1024) NOT NULL,
    msg_type        VARCHAR(32) NOT NULL COMMENT 'order/ship/system',
    read_flag       TINYINT NOT NULL DEFAULT 0,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_read (user_id, read_flag)
) ENGINE=InnoDB COMMENT='站内信';
