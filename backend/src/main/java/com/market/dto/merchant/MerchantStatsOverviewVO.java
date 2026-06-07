package com.market.dto.merchant;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MerchantStatsOverviewVO {

    private BigDecimal todaySalesAmount;
    private long todayOrderCount;
    private BigDecimal totalSalesAmount;
    private long totalOrderCount;
    private long pendingShipCount;
    /** 近 7 日去重下单用户数（暂无独立访客埋点，以买家数近似） */
    private long recentBuyerCount;
}
