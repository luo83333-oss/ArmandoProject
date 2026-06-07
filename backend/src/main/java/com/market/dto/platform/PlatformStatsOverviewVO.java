package com.market.dto.platform;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlatformStatsOverviewVO {

    private BigDecimal todayGmv;
    private long todayOrderCount;
    private BigDecimal totalGmv;
    private long totalOrderCount;
    private BigDecimal totalCommission;
    private long activeShopCount;
}
