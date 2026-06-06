package com.market.controller;

import com.market.common.Result;
import com.market.dto.cart.CartAddRequest;
import com.market.dto.cart.CartItemVO;
import com.market.dto.cart.CartUpdateRequest;
import com.market.security.UserContext;
import com.market.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.list(UserContext.getUserId()));
    }

    @PostMapping
    public Result<CartItemVO> add(@Valid @RequestBody CartAddRequest request) {
        return Result.ok(cartService.add(UserContext.getUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<CartItemVO> update(@PathVariable Long id, @Valid @RequestBody CartUpdateRequest request) {
        return Result.ok(cartService.update(UserContext.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }
}
