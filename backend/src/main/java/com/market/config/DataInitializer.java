package com.market.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.UserRole;
import com.market.entity.SysUser;
import com.market.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_PHONE = "13800000000";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, ADMIN_PHONE)
        );
        if (count == 0) {
            SysUser admin = new SysUser();
            admin.setPhone(ADMIN_PHONE);
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setNickname("平台管理员");
            admin.setStatus(1);
            admin.setRole(UserRole.ADMIN.getCode());
            userMapper.insert(admin);
            log.info("已创建平台管理员账号: {} / admin123", ADMIN_PHONE);
        }
    }
}
