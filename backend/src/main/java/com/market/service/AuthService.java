package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.common.UserRole;
import com.market.dto.auth.AuthResponse;
import com.market.dto.auth.UserVO;
import com.market.entity.SysUser;
import com.market.mapper.SysUserMapper;
import com.market.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String MOCK_CODE = "123456";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public void sendCode(String phone) {
        String code = MOCK_CODE;
        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + phone, code, Duration.ofMinutes(5));
    }

    public AuthResponse register(String phone, String password, String code) {
        verifyCode(phone, code);
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "手机号已注册");
        }
        SysUser user = new SysUser();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setStatus(1);
        user.setRole(UserRole.BUYER.getCode());
        userMapper.insert(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(String phone, String password) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已禁用");
        }
        return buildAuthResponse(user);
    }

    public AuthResponse wechatLogin(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        String mockOpenId = "wx_mock_" + code;
        String phone = "199" + String.format("%08d", Math.abs(mockOpenId.hashCode()) % 100_000_000);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) {
            user = new SysUser();
            user.setPhone(phone);
            user.setPasswordHash(passwordEncoder.encode("wx_" + mockOpenId));
            user.setNickname("微信用户");
            user.setStatus(1);
            user.setRole(UserRole.BUYER.getCode());
            userMapper.insert(user);
        }
        return buildAuthResponse(user);
    }

    public UserVO currentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toUserVO(user);
    }

    private void verifyCode(String phone, String code) {
        String cached = redisTemplate.opsForValue().get(CODE_KEY_PREFIX + phone);
        if (!MOCK_CODE.equals(code) && (cached == null || !cached.equals(code))) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "验证码错误或已过期");
        }
        redisTemplate.delete(CODE_KEY_PREFIX + phone);
    }

    private AuthResponse buildAuthResponse(SysUser user) {
        int role = user.getRole() != null ? user.getRole() : UserRole.BUYER.getCode();
        String token = jwtUtil.createToken(user.getId(), user.getPhone(), role);
        return AuthResponse.builder()
                .token(token)
                .user(toUserVO(user))
                .build();
    }

    private UserVO toUserVO(SysUser user) {
        return UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole() != null ? user.getRole() : UserRole.BUYER.getCode())
                .build();
    }
}
