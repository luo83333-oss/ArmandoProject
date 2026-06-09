package com.market.controller;

import com.market.common.Result;
import com.market.dto.engagement.FollowShopVO;
import com.market.dto.engagement.FollowStatusVO;
import com.market.dto.shop.ShopRankVO;
import com.market.security.UserContext;
import com.market.service.ShopFollowService;
import com.market.service.ShopRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRankService shopRankService;
    private final ShopFollowService shopFollowService;

    @GetMapping("/rank")
    public Result<List<ShopRankVO>> rank(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(shopRankService.listTop(limit));
    }

    @GetMapping("/following")
    public Result<List<FollowShopVO>> following() {
        return Result.ok(shopFollowService.listFollowing(UserContext.getUserId()));
    }

    @GetMapping("/{shopId}/followed")
    public Result<FollowStatusVO> followStatus(@PathVariable Long shopId) {
        return Result.ok(shopFollowService.status(UserContext.getUserId(), shopId));
    }

    @PostMapping("/{shopId}/follow")
    public Result<Void> follow(@PathVariable Long shopId) {
        shopFollowService.follow(UserContext.getUserId(), shopId);
        return Result.ok();
    }

    @DeleteMapping("/{shopId}/follow")
    public Result<Void> unfollow(@PathVariable Long shopId) {
        shopFollowService.unfollow(UserContext.getUserId(), shopId);
        return Result.ok();
    }
}
