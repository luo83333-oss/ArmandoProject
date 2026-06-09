package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_shop_follow")
public class UserShopFollow {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long shopId;
    private LocalDateTime createdAt;
}
