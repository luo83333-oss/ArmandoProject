package com.market.dto.platform;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RankConfigVO {

    private BigDecimal saleWeight;
    private BigDecimal reviewWeight;
    private BigDecimal violationPenalty;
}
