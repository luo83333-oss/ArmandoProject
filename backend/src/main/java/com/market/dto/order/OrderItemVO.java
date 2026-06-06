package com.market.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemVO {

    private Long id;
    private Long productId;
    private Long skuId;
    private String productTitle;
    private String specJson;
    private Integer quantity;
    private BigDecimal unitPrice;
}
