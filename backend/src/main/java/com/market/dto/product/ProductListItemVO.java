package com.market.dto.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductListItemVO {

    private Long id;
    private String title;
    private String mainImageUrl;
    private Long shopId;
    private String shopName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal minPrice;
    private Integer totalStock;
    private Integer shelfStatus;
    private Integer violationFlag;
}
