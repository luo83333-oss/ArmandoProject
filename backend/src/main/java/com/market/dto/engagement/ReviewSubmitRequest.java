package com.market.dto.engagement;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ReviewSubmitRequest {

    @NotNull(message = "请选择评分")
    @Min(value = 1, message = "评分为 1-5 星")
    @Max(value = 5, message = "评分为 1-5 星")
    private Integer rating;

    @Size(max = 1024, message = "评价内容不能超过 1024 字")
    private String content;
}
