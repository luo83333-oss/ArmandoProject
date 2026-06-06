package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.dto.product.CategoryVO;
import com.market.entity.ProductCategory;
import com.market.mapper.ProductCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProductCategoryMapper categoryMapper;

    public List<CategoryVO> listTree() {
        List<ProductCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder)
        );
        Map<Long, List<ProductCategory>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        return buildChildren(byParent, 0L);
    }

    public ProductCategory requireCategory(Long categoryId) {
        ProductCategory category = categoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new com.market.common.BusinessException(
                    com.market.common.ResultCode.BAD_REQUEST.getCode(), "类目不存在");
        }
        return category;
    }

    public String getCategoryName(Long categoryId) {
        ProductCategory category = categoryMapper.selectById(categoryId);
        return category != null ? category.getName() : null;
    }

    private List<CategoryVO> buildChildren(Map<Long, List<ProductCategory>> byParent, Long parentId) {
        List<ProductCategory> children = byParent.getOrDefault(parentId, List.of());
        List<CategoryVO> result = new ArrayList<>();
        for (ProductCategory c : children) {
            result.add(CategoryVO.builder()
                    .id(c.getId())
                    .parentId(c.getParentId())
                    .name(c.getName())
                    .children(buildChildren(byParent, c.getId()))
                    .build());
        }
        return result;
    }
}
