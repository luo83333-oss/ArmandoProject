package com.market.dto.cart;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class CartUpdateRequest {

    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;

    private Integer selected;
}
