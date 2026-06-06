package com.market.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    BUYER(1),
    MERCHANT(2),
    ADMIN(9);

    private final int code;

    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效用户角色");
    }
}
