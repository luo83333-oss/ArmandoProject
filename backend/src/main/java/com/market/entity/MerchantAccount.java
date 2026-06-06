package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant_account")
public class MerchantAccount {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String companyName;
    private String contactName;
    private String contactPhone;
    private String licenseNo;
    private String licenseFileUrl;
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
