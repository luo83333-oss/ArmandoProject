package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String name;
    private String logoUrl;
    private String description;
    private Integer status;
    private BigDecimal weightScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
