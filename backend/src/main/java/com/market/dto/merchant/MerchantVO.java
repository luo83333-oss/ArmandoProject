package com.market.dto.merchant;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MerchantVO {

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
    private Long shopId;
    private String shopName;
}
