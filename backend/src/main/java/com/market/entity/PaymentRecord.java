package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String channel;
    private String tradeNo;
    private BigDecimal payAmount;
    private Integer payStatus;
    private String callbackRaw;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
