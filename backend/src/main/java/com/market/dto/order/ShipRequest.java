package com.market.dto.order;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ShipRequest {

    @NotBlank(message = "物流单号不能为空")
    private String logisticsNo;
}
