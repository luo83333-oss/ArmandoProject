package com.market.controller;

import com.market.common.Result;
import com.market.dto.order.OrderVO;
import com.market.dto.order.ShipRequest;
import com.market.security.UserContext;
import com.market.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
@Validated
public class MerchantOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<List<OrderVO>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(orderService.listForMerchant(UserContext.getUserId(), status));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> get(@PathVariable Long id) {
        return Result.ok(orderService.getForMerchant(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/ship")
    public Result<OrderVO> ship(@PathVariable Long id, @Valid @RequestBody ShipRequest request) {
        return Result.ok(orderService.ship(UserContext.getUserId(), id, request.getLogisticsNo()));
    }
}
