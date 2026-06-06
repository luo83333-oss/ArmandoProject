package com.market.controller;

import com.market.common.Result;
import com.market.common.UserRole;
import com.market.dto.merchant.MerchantAuditRequest;
import com.market.dto.merchant.MerchantVO;
import com.market.security.RequireRole;
import com.market.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/platform/merchants")
@RequiredArgsConstructor
@Validated
public class PlatformMerchantController {

    private final MerchantService merchantService;

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public Result<List<MerchantVO>> list(@RequestParam(required = false) Integer auditStatus) {
        return Result.ok(merchantService.listForPlatform(auditStatus));
    }

    @PostMapping("/{id}/audit")
    @RequireRole(UserRole.ADMIN)
    public Result<MerchantVO> audit(@PathVariable Long id,
                                    @Valid @RequestBody MerchantAuditRequest request) {
        return Result.ok(merchantService.audit(id, request.getAction(), request.getRemark()));
    }
}
