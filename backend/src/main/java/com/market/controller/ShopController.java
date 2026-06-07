package com.market.controller;

import com.market.common.Result;
import com.market.dto.shop.ShopRankVO;
import com.market.service.ShopRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRankService shopRankService;

    @GetMapping("/rank")
    public Result<List<ShopRankVO>> rank(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(shopRankService.listTop(limit));
    }
}
