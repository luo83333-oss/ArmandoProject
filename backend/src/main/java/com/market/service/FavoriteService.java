package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.dto.engagement.FavoriteItemVO;
import com.market.dto.engagement.FavoriteStatusVO;
import com.market.entity.Product;
import com.market.entity.ProductSku;
import com.market.entity.Shop;
import com.market.entity.UserFavorite;
import com.market.mapper.ProductMapper;
import com.market.mapper.ShopMapper;
import com.market.mapper.UserFavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserFavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final ProductHelper productHelper;
    private final ShopMapper shopMapper;

    @Transactional
    public void add(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getShelfStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在或已下架");
        }
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getProductId, productId)
        );
        if (count > 0) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
    }

    @Transactional
    public void remove(Long userId, Long productId) {
        favoriteMapper.delete(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getProductId, productId)
        );
    }

    public List<FavoriteItemVO> listMine(Long userId) {
        List<UserFavorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreatedAt)
        );
        List<FavoriteItemVO> result = new ArrayList<>();
        for (UserFavorite fav : favorites) {
            Product product = productMapper.selectById(fav.getProductId());
            if (product == null) {
                continue;
            }
            Shop shop = shopMapper.selectById(product.getShopId());
            List<ProductSku> skus = productHelper.loadSkus(product.getId());
            BigDecimal minPrice = null;
            for (ProductSku sku : skus) {
                if (sku.getPrice() != null && (minPrice == null || sku.getPrice().compareTo(minPrice) < 0)) {
                    minPrice = sku.getPrice();
                }
            }
            result.add(FavoriteItemVO.builder()
                    .productId(product.getId())
                    .title(product.getTitle())
                    .mainImageUrl(product.getMainImageUrl())
                    .minPrice(minPrice)
                    .shopId(product.getShopId())
                    .shopName(shop != null ? shop.getName() : null)
                    .build());
        }
        return result;
    }

    public FavoriteStatusVO status(Long userId, Long productId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getProductId, productId)
        );
        return new FavoriteStatusVO(count > 0);
    }

    public boolean isFavorited(Long userId, Long productId) {
        if (userId == null) {
            return false;
        }
        return favoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getProductId, productId)
        ) > 0;
    }
}
