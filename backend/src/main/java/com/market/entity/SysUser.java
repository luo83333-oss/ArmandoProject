package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    /** 1买家 2商家 9平台管理员 */
    private Integer role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
