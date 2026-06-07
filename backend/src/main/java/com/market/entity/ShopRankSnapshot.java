package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("shop_rank_snapshot")
public class ShopRankSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private BigDecimal rankScore;
    private Integer rankPosition;
    private LocalDate snapshotDate;
    private LocalDateTime createdAt;
}
