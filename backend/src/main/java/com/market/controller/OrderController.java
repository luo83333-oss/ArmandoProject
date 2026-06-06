package com.market.controller;

import com.market.common.Result;
import com.market.dto.order.CheckoutRequest;
import com.market.dto.order.OrderVO;
import com.market.security.UserContext;
import com.market.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public Result<List<OrderVO>> checkout(@Valid @RequestBody CheckoutRequest request) {
        return Result.ok(orderService.checkout(UserContext.getUserId(), request));
    }

    @GetMapping
    public Result<List<OrderVO>> list() {
        return Result.ok(orderService.listMine(UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> get(@PathVariable Long id) {
        return Result.ok(orderService.getMine(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/pay")
    public Result<OrderVO> mockPay(@PathVariable Long id) {
        return Result.ok(orderService.mockPay(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/confirm")
    public Result<OrderVO> confirm(@PathVariable Long id) {
        return Result.ok(orderService.confirmReceive(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id) {
        return Result.ok(orderService.cancel(UserContext.getUserId(), id));
    }
}
