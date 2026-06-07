package com.market.dto.payment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SandboxCompleteRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String tradeNo;

    @NotBlank
    private String channel;
}
