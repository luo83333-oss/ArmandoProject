package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.common.BusinessException;
import com.market.common.PageResult;
import com.market.common.ResultCode;
import com.market.dto.product.ProductListItemVO;
import com.market.dto.product.ProductSaveRequest;
import com.market.dto.product.ProductVO;
import com.market.dto.product.SkuRequest;
import com.market.entity.Product;
import com.market.entity.ProductImage;
import com.market.entity.ProductSku;
import com.market.entity.Shop;
import com.market.mapper.ProductImageMapper;
import com.market.mapper.ProductMapper;
import com.market.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductImageMapper productImageMapper;
    private final MerchantShopService merchantShopService;
    private final CategoryService categoryService;
    private final ProductHelper productHelper;

    public PageResult<ProductListItemVO> listMine(Long userId, int page, int size, String keyword) {
        Shop shop = merchantShopService.requireApprovedShop(userId);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shop.getId())
                .orderByDesc(Product::getUpdatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getTitle, keyword.trim());
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

    public ProductVO getMine(Long userId, Long productId) {
        Shop shop = merchantShopService.requireApprovedShop(userId);
        Product product = requireShopProduct(shop.getId(), productId);
        return productHelper.toDetailVO(product, productHelper.loadSkus(product.getId()));
    }

    @Transactional
    public ProductVO create(Long userId, ProductSaveRequest request) {
        Shop shop = merchantShopService.requireApprovedShop(userId);
        categoryService.requireCategory(request.getCategoryId());

        Product product = new Product();
        product.setShopId(shop.getId());
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setMainImageUrl(request.getMainImageUrl());
        product.setDetailHtml(request.getDetailHtml());
        product.setShelfStatus(0);
        product.setViolationFlag(0);
        productMapper.insert(product);
        saveSkus(product.getId(), request.getSkus());
        saveGallery(product.getId(), request.getMainImageUrl(), request.getGalleryUrls());
        return productHelper.toDetailVO(product, productHelper.loadSkus(product.getId()));
    }

    @Transactional
    public ProductVO update(Long userId, Long productId, ProductSaveRequest request) {
        Shop shop = merchantShopService.requireApprovedShop(userId);
        Product product = requireShopProduct(shop.getId(), productId);
        categoryService.requireCategory(request.getCategoryId());

        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setMainImageUrl(request.getMainImageUrl());
        product.setDetailHtml(request.getDetailHtml());
        productMapper.updateById(product);

        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
        saveSkus(productId, request.getSkus());
        saveGallery(productId, request.getMainImageUrl(), request.getGalleryUrls());
        return productHelper.toDetailVO(productMapper.selectById(productId), productHelper.loadSkus(productId));
    }

    @Transactional
    public void delete(Long userId, Long productId) {
        Shop shop = merchantShopService.requireApprovedShop(userId);
        requireShopProduct(shop.getId(), productId);
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId));
        productMapper.deleteById(productId);
    }

    public ProductVO updateShelf(Long userId, Long productId, Integer shelfStatus) {
        if (shelfStatus == null || (shelfStatus != 0 && shelfStatus != 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "上下架状态无效");
        }
        Shop shop = merchantShopService.requireApprovedShop(userId);
        Product product = requireShopProduct(shop.getId(), productId);
        if (shelfStatus == 1 && product.getViolationFlag() != null && product.getViolationFlag() == 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "商品违规待审，无法上架");
        }
        product.setShelfStatus(shelfStatus);
        productMapper.updateById(product);
        return productHelper.toDetailVO(product, productHelper.loadSkus(product.getId()));
    }

    private Product requireShopProduct(Long shopId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !shopId.equals(product.getShopId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        return product;
    }

    private void saveSkus(Long productId, List<SkuRequest> skus) {
        for (SkuRequest skuReq : skus) {
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSpecJson(skuReq.getSpecJson());
            sku.setPrice(skuReq.getPrice());
            sku.setStock(skuReq.getStock());
            sku.setSkuCode(skuReq.getSkuCode());
            sku.setStatus(1);
            skuMapper.insert(sku);
        }
    }

    private void saveGallery(Long productId, String mainImageUrl, List<String> galleryUrls) {
        List<String> auxUrls = normalizeGalleryUrls(galleryUrls);
        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId));
        if (StringUtils.hasText(mainImageUrl)) {
            ProductImage main = new ProductImage();
            main.setProductId(productId);
            main.setUrl(mainImageUrl.trim());
            main.setSortOrder(0);
            productImageMapper.insert(main);
        }
        int order = 1;
        for (String url : auxUrls) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(url);
            image.setSortOrder(order++);
            productImageMapper.insert(image);
        }
    }

    private List<String> normalizeGalleryUrls(List<String> galleryUrls) {
        if (galleryUrls == null || galleryUrls.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String url : galleryUrls) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            result.add(url.trim());
            if (result.size() >= 5) {
                break;
            }
        }
        return result;
    }
}
