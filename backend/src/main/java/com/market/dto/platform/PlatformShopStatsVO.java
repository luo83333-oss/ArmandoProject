package com.market.dto.platform;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlatformShopStatsVO {

    private int rank;
    private Long shopId;
    private String shopName;
    private BigDecimal gmvAmount;
    private long orderCount;
    private BigDecimal commissionAmount;
}
