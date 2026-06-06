package com.market.controller;

import com.market.common.Result;
import com.market.dto.auth.*;
import com.market.security.UserContext;
import com.market.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendCode(request.getPhone());
        return Result.ok();
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(
                request.getPhone(), request.getPassword(), request.getCode()));
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request.getPhone(), request.getPassword()));
    }

    @PostMapping("/wechat")
    public Result<AuthResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return Result.ok(authService.wechatLogin(request.getCode()));
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.currentUser(UserContext.getUserId()));
    }
}
