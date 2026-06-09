package com.market.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.common.BusinessException;
import com.market.common.Result;
import com.market.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            writeError(response, new BusinessException(ResultCode.UNAUTHORIZED));
            return false;
        }
        String token = auth.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            String phone = claims.get("phone", String.class);
            int role = claims.get("role", Integer.class);
            UserContext.set(new LoginUser(userId, phone, role));
            return true;
        } catch (ExpiredJwtException e) {
            writeError(response, new BusinessException(ResultCode.UNAUTHORIZED));
            return false;
        } catch (JwtException | NumberFormatException e) {
            writeError(response, new BusinessException(ResultCode.UNAUTHORIZED));
            return false;
        }
    }

    private void writeError(HttpServletResponse response, BusinessException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(ex.getCode(), ex.getMessage())));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
