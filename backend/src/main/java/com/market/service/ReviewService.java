package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.OrderStatus;
import com.market.common.ResultCode;
import com.market.dto.engagement.ReviewSubmitRequest;
import com.market.dto.engagement.ReviewVO;
import com.market.entity.OrderItem;
import com.market.entity.ProductReview;
import com.market.entity.UserOrder;
import com.market.mapper.OrderItemMapper;
import com.market.mapper.ProductReviewMapper;
import com.market.mapper.UserOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewMapper reviewMapper;
    private final UserOrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShopRankService shopRankService;

    @Transactional
    public ReviewVO submit(Long userId, Long orderId, ReviewSubmitRequest request) {
        UserOrder order = orderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getStatus() != OrderStatus.COMPLETED.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "仅已完成订单可评价");
        }
        Long existing = reviewMapper.selectCount(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getOrderId, orderId)
        );
        if (existing > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该订单已评价");
        }
        OrderItem firstItem = orderItemMapper.selectOne(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .last("LIMIT 1")
        );
        if (firstItem == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "订单无商品明细");
        }

        ProductReview review = new ProductReview();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setProductId(firstItem.getProductId());
        review.setShopId(order.getShopId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        reviewMapper.insert(review);
        shopRankService.refreshShop(order.getShopId());

        return toVO(review);
    }

    public boolean hasReview(Long orderId) {
        if (orderId == null) {
            return false;
        }
        return reviewMapper.selectCount(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getOrderId, orderId)
        ) > 0;
    }

    public ReviewVO getByOrderId(Long orderId) {
        ProductReview review = reviewMapper.selectOne(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getOrderId, orderId)
        );
        return review == null ? null : toVO(review);
    }

    private ReviewVO toVO(ProductReview review) {
        return ReviewVO.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .productId(review.getProductId())
                .shopId(review.getShopId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
