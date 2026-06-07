package com.market.controller;

import com.market.common.Result;
import com.market.dto.merchant.MerchantStatsOverviewVO;
import com.market.dto.merchant.MerchantStatsTrendPointVO;
import com.market.security.UserContext;
import com.market.service.MerchantStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/stats")
@RequiredArgsConstructor
public class MerchantStatsController {

    private final MerchantStatsService merchantStatsService;

    @GetMapping("/overview")
    public Result<MerchantStatsOverviewVO> overview() {
        return Result.ok(merchantStatsService.getOverview(UserContext.getUserId()));
    }

    @GetMapping("/trend")
    public Result<List<MerchantStatsTrendPointVO>> trend(
            @RequestParam(defaultValue = "7") int days) {
        return Result.ok(merchantStatsService.getTrend(UserContext.getUserId(), days));
    }
}
