package com.market.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("platform_config")
public class PlatformConfig {

    @TableId
    private String configKey;
    private String configValue;
    private String remark;
    private LocalDateTime updatedAt;
}
