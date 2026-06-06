package com.market.dto.order;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class CheckoutRequest {

    @NotNull(message = "收货地址不能为空")
    @Valid
    private AddressRequest address;
}
