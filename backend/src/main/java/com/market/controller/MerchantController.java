package com.market.controller;

import com.market.common.Result;
import com.market.dto.merchant.MerchantApplyRequest;
import com.market.dto.merchant.MerchantVO;
import com.market.security.UserContext;
import com.market.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@Validated
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/apply")
    public Result<MerchantVO> apply(@Valid @RequestBody MerchantApplyRequest request) {
        return Result.ok(merchantService.apply(UserContext.getUserId(), request));
    }

    @GetMapping("/mine")
    public Result<MerchantVO> mine() {
        return Result.ok(merchantService.getMine(UserContext.getUserId()));
    }
}
