package com.market.dto.platform;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlatformStatsTrendPointVO {

    private String period;
    private BigDecimal gmvAmount;
    private long orderCount;
    private BigDecimal commissionAmount;
}
