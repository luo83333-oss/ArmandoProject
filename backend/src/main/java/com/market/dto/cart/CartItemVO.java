package com.market.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemVO {

    private Long id;
    private Long skuId;
    private Long productId;
    private String productTitle;
    private String mainImageUrl;
    private String specJson;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Integer selected;
    private Long shopId;
    private String shopName;
}
