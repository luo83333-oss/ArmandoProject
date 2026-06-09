package com.market.dto.engagement;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewVO {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long shopId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
