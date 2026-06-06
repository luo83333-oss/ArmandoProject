package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.common.BusinessException;
import com.market.common.PageResult;
import com.market.common.ResultCode;
import com.market.dto.product.ProductListItemVO;
import com.market.dto.product.ProductVO;
import com.market.entity.Product;
import com.market.entity.ProductCategory;
import com.market.entity.ProductSku;
import com.market.mapper.ProductCategoryMapper;
import com.market.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductHelper productHelper;

    public PageResult<ProductListItemVO> listPublic(String keyword, Long categoryId, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getShelfStatus, 1)
                .eq(Product::getViolationFlag, 0)
                .orderByDesc(Product::getUpdatedAt);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getTitle, keyword.trim());
        }
        if (categoryId != null) {
            List<Long> categoryIds = collectCategoryIds(categoryId);
            if (!categoryIds.isEmpty()) {
                wrapper.in(Product::getCategoryId, categoryIds);
            }
        }

        Page<Product> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        List<Long> productIds = result.getRecords().stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, List<ProductSku>> skuMap = productHelper.loadSkusBatch(productIds);
        List<ProductListItemVO> records = result.getRecords().stream()
                .map(p -> productHelper.toListItemVO(p, skuMap.getOrDefault(p.getId(), List.of())))
                .collect(Collectors.toList());

        return PageResult.<ProductListItemVO>builder()
                .records(records)
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .build();
    }

    public ProductVO getPublic(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null
                || product.getShelfStatus() == null || product.getShelfStatus() != 1
                || product.getViolationFlag() != null && product.getViolationFlag() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在或已下架");
        }
        return productHelper.toDetailVO(product, productHelper.loadSkus(product.getId()));
    }

    private List<Long> collectCategoryIds(Long categoryId) {
        List<ProductCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>().eq(ProductCategory::getStatus, 1)
        );
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        collectDescendants(all, categoryId, ids);
        return ids;
    }

    private void collectDescendants(List<ProductCategory> all, Long parentId, List<Long> ids) {
        for (ProductCategory c : all) {
            if (parentId.equals(c.getParentId())) {
                ids.add(c.getId());
                collectDescendants(all, c.getId(), ids);
            }
        }
    }
}
