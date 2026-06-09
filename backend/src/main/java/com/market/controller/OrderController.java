package com.market.controller;

import com.market.common.Result;
import com.market.dto.engagement.ReviewSubmitRequest;
import com.market.dto.engagement.ReviewVO;
import com.market.dto.order.CheckoutRequest;
import com.market.dto.order.OrderVO;
import com.market.dto.payment.PayRequest;
import com.market.dto.payment.PayResultVO;
import com.market.security.UserContext;
import com.market.service.OrderService;
import com.market.service.PaymentService;
import com.market.service.ReviewService;
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
    private final PaymentService paymentService;
    private final ReviewService reviewService;

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
    public Result<PayResultVO> pay(@PathVariable Long id,
                                   @RequestBody(required = false) @Valid PayRequest request) {
        String channel = request != null && request.getChannel() != null ? request.getChannel() : "mock";
        return Result.ok(paymentService.pay(UserContext.getUserId(), id, channel));
    }

    @PostMapping("/{id}/confirm")
    public Result<OrderVO> confirm(@PathVariable Long id) {
        return Result.ok(orderService.confirmReceive(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id) {
        return Result.ok(orderService.cancel(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/review")
    public Result<ReviewVO> review(@PathVariable Long id, @Valid @RequestBody ReviewSubmitRequest request) {
        return Result.ok(reviewService.submit(UserContext.getUserId(), id, request));
    }
}
