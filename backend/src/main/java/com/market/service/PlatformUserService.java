package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.common.UserRole;
import com.market.dto.platform.PlatformUserVO;
import com.market.entity.SysUser;
import com.market.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformUserService {

    private final SysUserMapper userMapper;

    public List<PlatformUserVO> list() {
        return userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreatedAt)
        ).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public PlatformUserVO updateStatus(Long operatorId, Long userId, Integer status) {
        if (operatorId.equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能修改自己的账号状态");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        if (user.getRole() != null && user.getRole() == UserRole.ADMIN.getCode()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "不能禁用平台管理员");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return toVO(user);
    }

    private PlatformUserVO toVO(SysUser user) {
        return PlatformUserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole())
                .roleLabel(roleLabel(user.getRole()))
                .status(user.getStatus())
                .statusLabel(user.getStatus() != null && user.getStatus() == 1 ? "正常" : "已禁用")
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String roleLabel(Integer role) {
        if (role == null) {
            return "未知";
        }
        if (role == UserRole.ADMIN.getCode()) {
            return "管理员";
        }
        if (role == UserRole.MERCHANT.getCode()) {
            return "商家";
        }
        return "买家";
    }
}
