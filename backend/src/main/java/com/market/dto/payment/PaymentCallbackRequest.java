package com.market.dto.payment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentCallbackRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String tradeNo;

    @NotNull
    private BigDecimal payAmount;

    @NotBlank
    private String sign;
}
