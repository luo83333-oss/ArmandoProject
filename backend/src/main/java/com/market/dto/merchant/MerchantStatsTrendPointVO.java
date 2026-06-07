package com.market.dto.merchant;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MerchantStatsTrendPointVO {

    private String date;
    private BigDecimal salesAmount;
    private long orderCount;
    private long buyerCount;
}
