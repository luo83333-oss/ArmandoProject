package com.market.controller;

import com.market.common.PageResult;
import com.market.common.Result;
import com.market.dto.product.ProductListItemVO;
import com.market.dto.product.ProductSaveRequest;
import com.market.dto.product.ProductVO;
import com.market.security.UserContext;
import com.market.service.MerchantProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
@Validated
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    @GetMapping
    public Result<PageResult<ProductListItemVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(merchantProductService.listMine(UserContext.getUserId(), page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> get(@PathVariable Long id) {
        return Result.ok(merchantProductService.getMine(UserContext.getUserId(), id));
    }

    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductSaveRequest request) {
        return Result.ok(merchantProductService.create(UserContext.getUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        return Result.ok(merchantProductService.update(UserContext.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        merchantProductService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }

    @PatchMapping("/{id}/shelf")
    public Result<ProductVO> updateShelf(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return Result.ok(merchantProductService.updateShelf(
                UserContext.getUserId(), id, body.get("shelfStatus")));
    }
}
