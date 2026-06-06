package com.market.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {

    private Long id;
    private String phone;
    private String nickname;
    private Integer role;
}
