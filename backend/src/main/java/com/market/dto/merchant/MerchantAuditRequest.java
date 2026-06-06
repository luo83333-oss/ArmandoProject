package com.market.dto.merchant;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MerchantAuditRequest {

    /** 1通过 2驳回 3冻结 */
    @NotNull(message = "审核动作不能为空")
    private Integer action;

    private String remark;
}
