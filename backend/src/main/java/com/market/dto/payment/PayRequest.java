package com.market.dto.payment;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PayRequest {

    @NotBlank
    private String channel;
}
