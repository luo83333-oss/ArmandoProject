package com.market.dto.platform;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class RankConfigUpdateRequest {

    @NotNull
    @DecimalMin("0")
    private BigDecimal saleWeight;

    @NotNull
    @DecimalMin("0")
    private BigDecimal reviewWeight;

    @NotNull
    @DecimalMin("0")
    private BigDecimal violationPenalty;
}
