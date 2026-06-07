package com.market.controller;

import com.market.common.Result;
import com.market.common.UserRole;
import com.market.dto.platform.RankConfigUpdateRequest;
import com.market.dto.platform.RankConfigVO;
import com.market.dto.shop.ShopRankVO;
import com.market.security.RequireRole;
import com.market.service.ShopRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/platform/rank")
@RequiredArgsConstructor
@Validated
public class PlatformRankController {

    private final ShopRankService shopRankService;

    @GetMapping("/config")
    @RequireRole(UserRole.ADMIN)
    public Result<RankConfigVO> getConfig() {
        return Result.ok(shopRankService.getConfig());
    }

    @PutMapping("/config")
    @RequireRole(UserRole.ADMIN)
    public Result<RankConfigVO> updateConfig(@Valid @RequestBody RankConfigUpdateRequest request) {
        return Result.ok(shopRankService.updateConfig(request));
    }

    @PostMapping("/recalculate")
    @RequireRole(UserRole.ADMIN)
    public Result<List<ShopRankVO>> recalculate() {
        return Result.ok(shopRankService.recalculateAll());
    }

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public Result<List<ShopRankVO>> list(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(shopRankService.listTop(limit));
    }
}
