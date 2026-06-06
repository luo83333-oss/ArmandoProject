package com.market.dto.platform;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlatformUserVO {

    private Long id;
    private String phone;
    private String nickname;
    private Integer role;
    private String roleLabel;
    private Integer status;
    private String statusLabel;
    private LocalDateTime createdAt;
}
