package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product_sku")
public class ProductSku {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String specJson;
    private java.math.BigDecimal price;
    private Integer stock;
    private String skuCode;
    private Integer status;
}
