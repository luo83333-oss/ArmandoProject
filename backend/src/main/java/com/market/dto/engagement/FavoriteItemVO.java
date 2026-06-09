package com.market.dto.engagement;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FavoriteItemVO {

    private Long productId;
    private String title;
    private String mainImageUrl;
    private BigDecimal minPrice;
    private Long shopId;
    private String shopName;
}
