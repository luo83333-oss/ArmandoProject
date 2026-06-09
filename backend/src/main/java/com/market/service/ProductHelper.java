package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.dto.product.ProductListItemVO;
import com.market.dto.product.ProductVO;
import com.market.dto.product.SkuVO;
import com.market.entity.Product;
import com.market.entity.ProductImage;
import com.market.entity.ProductSku;
import com.market.entity.Shop;
import com.market.mapper.ProductImageMapper;
import com.market.mapper.ProductSkuMapper;
import com.market.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductHelper {

    private final ProductSkuMapper skuMapper;
    private final ProductImageMapper productImageMapper;
    private final ShopMapper shopMapper;
    private final CategoryService categoryService;

    public List<ProductSku> loadSkus(Long productId) {
        return skuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .eq(ProductSku::getStatus, 1)
        );
    }

    public Map<Long, List<ProductSku>> loadSkusBatch(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return skuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .in(ProductSku::getProductId, productIds)
                        .eq(ProductSku::getStatus, 1)
        ).stream().collect(Collectors.groupingBy(ProductSku::getProductId));
    }

    public List<String> loadGalleryUrls(Long productId) {
        return productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .gt(ProductImage::getSortOrder, 0)
                        .orderByAsc(ProductImage::getSortOrder)
        ).stream().map(ProductImage::getUrl).collect(Collectors.toList());
    }

    public ProductVO toDetailVO(Product product, List<ProductSku> skus) {
        return toDetailVO(product, skus, loadGalleryUrls(product.getId()));
    }

    public ProductVO toDetailVO(Product product, List<ProductSku> skus, List<String> galleryUrls) {
        Shop shop = shopMapper.selectById(product.getShopId());
        BigDecimal minPrice = null;
        int totalStock = 0;
        List<SkuVO> skuVOs = skus.stream().map(this::toSkuVO).collect(Collectors.toList());
        for (ProductSku sku : skus) {
            totalStock += sku.getStock() != null ? sku.getStock() : 0;
            if (sku.getPrice() != null && (minPrice == null || sku.getPrice().compareTo(minPrice) < 0)) {
                minPrice = sku.getPrice();
            }
        }
        return ProductVO.builder()
                .id(product.getId())
                .shopId(product.getShopId())
                .shopName(shop != null ? shop.getName() : null)
                .categoryId(product.getCategoryId())
                .categoryName(categoryService.getCategoryName(product.getCategoryId()))
                .title(product.getTitle())
                .mainImageUrl(product.getMainImageUrl())
                .galleryUrls(galleryUrls)
                .detailHtml(product.getDetailHtml())
                .shelfStatus(product.getShelfStatus())
                .violationFlag(product.getViolationFlag())
                .minPrice(minPrice)
                .totalStock(totalStock)
                .skus(skuVOs)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public ProductListItemVO toListItemVO(Product product, List<ProductSku> skus) {
        Shop shop = shopMapper.selectById(product.getShopId());
        BigDecimal minPrice = null;
        int totalStock = 0;
        for (ProductSku sku : skus) {
            totalStock += sku.getStock() != null ? sku.getStock() : 0;
            if (sku.getPrice() != null && (minPrice == null || sku.getPrice().compareTo(minPrice) < 0)) {
                minPrice = sku.getPrice();
            }
        }
        return ProductListItemVO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .mainImageUrl(product.getMainImageUrl())
                .shopId(product.getShopId())
                .shopName(shop != null ? shop.getName() : null)
                .categoryId(product.getCategoryId())
                .categoryName(categoryService.getCategoryName(product.getCategoryId()))
                .minPrice(minPrice)
                .totalStock(totalStock)
                .shelfStatus(product.getShelfStatus())
                .violationFlag(product.getViolationFlag())
                .build();
    }

    private SkuVO toSkuVO(ProductSku sku) {
        return SkuVO.builder()
                .id(sku.getId())
                .specJson(sku.getSpecJson())
                .price(sku.getPrice())
                .stock(sku.getStock())
                .skuCode(sku.getSkuCode())
                .build();
    }
}
