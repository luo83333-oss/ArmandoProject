package com.market.security;

import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.common.UserRole;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        boolean allowed = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.getCode() == user.getRole());
        if (!allowed) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
