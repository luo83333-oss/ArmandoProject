package com.market.controller;

import com.market.common.PageResult;
import com.market.common.Result;
import com.market.dto.product.ProductListItemVO;
import com.market.dto.product.ProductVO;
import com.market.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public Result<PageResult<ProductListItemVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return Result.ok(productQueryService.listPublic(keyword, categoryId, page, size));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> get(@PathVariable Long id) {
        return Result.ok(productQueryService.getPublic(id));
    }
}
