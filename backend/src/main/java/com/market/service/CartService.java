package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.dto.cart.CartAddRequest;
import com.market.dto.cart.CartItemVO;
import com.market.dto.cart.CartUpdateRequest;
import com.market.entity.CartItem;
import com.market.entity.Product;
import com.market.entity.ProductSku;
import com.market.entity.Shop;
import com.market.mapper.CartItemMapper;
import com.market.mapper.ProductMapper;
import com.market.mapper.ProductSkuMapper;
import com.market.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;
    private final ProductQueryService productQueryService;

    public List<CartItemVO> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getUpdatedAt)
        );
        return items.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public CartItemVO add(Long userId, CartAddRequest request) {
        ProductSku sku = requireAvailableSku(request.getSkuId());

        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSkuId, request.getSkuId())
        );
        if (existing != null) {
            int newQty = existing.getQuantity() + request.getQuantity();
            if (newQty > sku.getStock()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存不足");
            }
            existing.setQuantity(newQty);
            existing.setSelected(1);
            cartItemMapper.updateById(existing);
            return toVO(existing);
        }

        if (request.getQuantity() > sku.getStock()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存不足");
        }
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setSkuId(request.getSkuId());
        item.setQuantity(request.getQuantity());
        item.setSelected(1);
        cartItemMapper.insert(item);
        return toVO(item);
    }

    public CartItemVO update(Long userId, Long cartItemId, CartUpdateRequest request) {
        CartItem item = requireUserCartItem(userId, cartItemId);
        if (request.getQuantity() != null) {
            ProductSku sku = requireAvailableSku(item.getSkuId());
            if (request.getQuantity() > sku.getStock()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存不足");
            }
            item.setQuantity(request.getQuantity());
        }
        if (request.getSelected() != null) {
            item.setSelected(request.getSelected() == 1 ? 1 : 0);
        }
        cartItemMapper.updateById(item);
        return toVO(item);
    }

    public void delete(Long userId, Long cartItemId) {
        requireUserCartItem(userId, cartItemId);
        cartItemMapper.deleteById(cartItemId);
    }

    public List<CartItem> listSelectedForCheckout(Long userId) {
        return cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSelected, 1)
        );
    }

    private CartItem requireUserCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        }
        return item;
    }

    private ProductSku requireAvailableSku(Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "SKU 不存在");
        }
        Product product = productMapper.selectById(sku.getProductId());
        if (product == null || product.getShelfStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品已下架");
        }
        try {
            productQueryService.getPublic(product.getId());
        } catch (BusinessException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品不可购买");
        }
        return sku;
    }

    private CartItemVO toVO(CartItem item) {
        ProductSku sku = skuMapper.selectById(item.getSkuId());
        Product product = sku != null ? productMapper.selectById(sku.getProductId()) : null;
        Shop shop = product != null ? shopMapper.selectById(product.getShopId()) : null;
        return CartItemVO.builder()
                .id(item.getId())
                .skuId(item.getSkuId())
                .productId(product != null ? product.getId() : null)
                .productTitle(product != null ? product.getTitle() : null)
                .mainImageUrl(product != null ? product.getMainImageUrl() : null)
                .specJson(sku != null ? sku.getSpecJson() : null)
                .price(sku != null ? sku.getPrice() : null)
                .stock(sku != null ? sku.getStock() : 0)
                .quantity(item.getQuantity())
                .selected(item.getSelected())
                .shopId(product != null ? product.getShopId() : null)
                .shopName(shop != null ? shop.getName() : null)
                .build();
    }
}
