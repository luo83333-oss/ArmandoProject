package com.market.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.UserRole;
import com.market.entity.ProductCategory;
import com.market.entity.SysUser;
import com.market.mapper.ProductCategoryMapper;
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
    private final ProductCategoryMapper categoryMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedCategories();
    }

    private void seedAdmin() {
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

    private void seedCategories() {
        Long count = categoryMapper.selectCount(null);
        if (count > 0) {
            return;
        }
        long digital = insertCategory(0L, "数码电子", 1);
        insertCategory(digital, "手机", 1);
        insertCategory(digital, "电脑", 2);
        long clothing = insertCategory(0L, "服装", 2);
        insertCategory(clothing, "男装", 1);
        insertCategory(clothing, "女装", 2);
        long food = insertCategory(0L, "食品", 3);
        insertCategory(food, "零食", 1);
        insertCategory(food, "饮料", 2);
        log.info("已初始化商品类目种子数据");
    }

    private long insertCategory(Long parentId, String name, int sortOrder) {
        ProductCategory category = new ProductCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setSortOrder(sortOrder);
        category.setStatus(1);
        categoryMapper.insert(category);
        return category.getId();
    }
}
