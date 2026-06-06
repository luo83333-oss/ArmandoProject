package com.market.dto.platform;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UpdateUserStatusRequest {

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;
}
