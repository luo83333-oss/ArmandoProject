-- 商品图片库：sort_order 0=主图，1-5=辅图
CREATE TABLE product_image (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT UNSIGNED NOT NULL,
    url         VARCHAR(512) NOT NULL,
    sort_order  TINYINT NOT NULL DEFAULT 0 COMMENT '0=主图 1-5=辅图',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_product (product_id)
) ENGINE=InnoDB COMMENT='商品图片';
