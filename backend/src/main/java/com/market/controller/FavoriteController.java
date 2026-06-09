package com.market.controller;

import com.market.common.Result;
import com.market.dto.engagement.FavoriteItemVO;
import com.market.dto.engagement.FavoriteStatusVO;
import com.market.security.UserContext;
import com.market.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Result<List<FavoriteItemVO>> list() {
        return Result.ok(favoriteService.listMine(UserContext.getUserId()));
    }

    @GetMapping("/products/{productId}/status")
    public Result<FavoriteStatusVO> status(@PathVariable Long productId) {
        return Result.ok(favoriteService.status(UserContext.getUserId(), productId));
    }

    @PostMapping("/products/{productId}")
    public Result<Void> add(@PathVariable Long productId) {
        favoriteService.add(UserContext.getUserId(), productId);
        return Result.ok();
    }

    @DeleteMapping("/products/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        favoriteService.remove(UserContext.getUserId(), productId);
        return Result.ok();
    }
}
