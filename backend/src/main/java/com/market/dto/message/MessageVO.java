package com.market.dto.message;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageVO {

    private Long id;
    private String title;
    private String content;
    private String msgType;
    private Boolean read;
    private Long orderId;
    private LocalDateTime createdAt;
}
