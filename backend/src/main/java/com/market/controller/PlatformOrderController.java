package com.market.controller;

import com.market.common.Result;
import com.market.common.UserRole;
import com.market.dto.order.OrderVO;
import com.market.security.RequireRole;
import com.market.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform/orders")
@RequiredArgsConstructor
@Validated
public class PlatformOrderController {

    private final OrderService orderService;

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public Result<List<OrderVO>> list(@RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) Long shopId) {
        return Result.ok(orderService.listForPlatform(status, shopId));
    }

    @GetMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public Result<OrderVO> get(@PathVariable Long id) {
        return Result.ok(orderService.getForPlatform(id));
    }
}
