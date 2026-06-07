package com.market.dto.shop;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShopRankVO {

    private Integer rank;
    private Long shopId;
    private String shopName;
    private String logoUrl;
    private String description;
    private BigDecimal weightScore;
    private Long saleCount;
    private Long reviewCount;
    private Long violationCount;
}
