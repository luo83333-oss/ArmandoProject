package com.market.controller;

import com.market.common.Result;
import com.market.dto.payment.PayResultVO;
import com.market.dto.payment.PaymentCallbackRequest;
import com.market.dto.payment.PaymentChannelVO;
import com.market.dto.payment.SandboxCompleteRequest;
import com.market.security.UserContext;
import com.market.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/channels")
    public Result<List<PaymentChannelVO>> channels() {
        return Result.ok(paymentService.listChannels());
    }

    @PostMapping("/sandbox/complete")
    public Result<PayResultVO> completeSandbox(@Valid @RequestBody SandboxCompleteRequest request) {
        return Result.ok(paymentService.completeSandbox(
                UserContext.getUserId(),
                request.getOrderId(),
                request.getTradeNo(),
                request.getChannel()
        ));
    }

    @PostMapping("/callback/{channel}")
    public Result<Void> callback(@PathVariable String channel,
                                 @Valid @RequestBody PaymentCallbackRequest request) {
        paymentService.handleCallback(channel, request);
        return Result.ok();
    }
}
