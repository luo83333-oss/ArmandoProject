package com.market.dto.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SkuVO {

    private Long id;
    private String specJson;
    private BigDecimal price;
    private Integer stock;
    private String skuCode;
}
