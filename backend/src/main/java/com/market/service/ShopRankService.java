package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.MerchantAuditStatus;
import com.market.common.OrderStatus;
import com.market.dto.platform.RankConfigVO;
import com.market.dto.platform.RankConfigUpdateRequest;
import com.market.dto.shop.ShopRankVO;
import com.market.entity.MerchantAccount;
import com.market.entity.PlatformConfig;
import com.market.entity.Product;
import com.market.entity.Shop;
import com.market.entity.ShopRankSnapshot;
import com.market.entity.UserOrder;
import com.market.mapper.MerchantAccountMapper;
import com.market.mapper.PlatformConfigMapper;
import com.market.mapper.ProductMapper;
import com.market.mapper.ProductReviewMapper;
import com.market.mapper.ShopMapper;
import com.market.mapper.ShopRankSnapshotMapper;
import com.market.mapper.UserOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopRankService {

    private static final String REDIS_RANK_KEY = "market:shop:rank";
    private static final String KEY_SALE_WEIGHT = "rank.sale_weight";
    private static final String KEY_REVIEW_WEIGHT = "rank.review_weight";
    private static final String KEY_VIOLATION_PENALTY = "rank.violation_penalty";

    private final ShopMapper shopMapper;
    private final MerchantAccountMapper merchantMapper;
    private final UserOrderMapper orderMapper;
    private final ProductReviewMapper reviewMapper;
    private final ProductMapper productMapper;
    private final PlatformConfigMapper configMapper;
    private final ShopRankSnapshotMapper snapshotMapper;
    private final StringRedisTemplate redisTemplate;

    public List<ShopRankVO> listTop(int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(REDIS_RANK_KEY, 0, size - 1);
        if (tuples == null || tuples.isEmpty()) {
            return listFromDbFallback(size);
        }
        List<Long> shopIds = new ArrayList<>();
        Map<Long, Double> scoreMap = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long shopId = Long.parseLong(tuple.getValue());
            shopIds.add(shopId);
            scoreMap.put(shopId, tuple.getScore() != null ? tuple.getScore() : 0D);
        }
        return buildRankList(shopIds, scoreMap);
    }

    public RankConfigVO getConfig() {
        return RankConfigVO.builder()
                .saleWeight(getConfigDecimal(KEY_SALE_WEIGHT, new BigDecimal("1.0")))
                .reviewWeight(getConfigDecimal(KEY_REVIEW_WEIGHT, new BigDecimal("0.5")))
                .violationPenalty(getConfigDecimal(KEY_VIOLATION_PENALTY, new BigDecimal("10")))
                .build();
    }

    @Transactional
    public RankConfigVO updateConfig(RankConfigUpdateRequest request) {
        upsertConfig(KEY_SALE_WEIGHT, request.getSaleWeight().toPlainString());
        upsertConfig(KEY_REVIEW_WEIGHT, request.getReviewWeight().toPlainString());
        upsertConfig(KEY_VIOLATION_PENALTY, request.getViolationPenalty().toPlainString());
        return getConfig();
    }

    @Transactional
    public List<ShopRankVO> recalculateAll() {
        RankConfigVO config = getConfig();
        List<Shop> shops = listActiveShops();
        Map<Long, Long> saleMap = countSalesByShop();
        Map<Long, Long> reviewMap = countReviewsByShop();
        Map<Long, Long> violationMap = countViolationsByShop();

        List<ShopScore> scores = new ArrayList<>();
        for (Shop shop : shops) {
            long sales = saleMap.getOrDefault(shop.getId(), 0L);
            long reviews = reviewMap.getOrDefault(shop.getId(), 0L);
            long violations = violationMap.getOrDefault(shop.getId(), 0L);
            BigDecimal weight = config.getSaleWeight().multiply(BigDecimal.valueOf(sales))
                    .add(config.getReviewWeight().multiply(BigDecimal.valueOf(reviews)))
                    .subtract(config.getViolationPenalty().multiply(BigDecimal.valueOf(violations)))
                    .setScale(4, RoundingMode.HALF_UP);
            if (weight.compareTo(BigDecimal.ZERO) < 0) {
                weight = BigDecimal.ZERO;
            }
            shop.setWeightScore(weight);
            shopMapper.updateById(shop);
            scores.add(new ShopScore(shop, weight, sales, reviews, violations));
        }

        scores.sort(Comparator.comparing(ShopScore::getWeight).reversed());

        redisTemplate.delete(REDIS_RANK_KEY);
        for (ShopScore item : scores) {
            redisTemplate.opsForZSet().add(REDIS_RANK_KEY, String.valueOf(item.shop.getId()),
                    item.weight.doubleValue());
        }

        saveSnapshot(scores);
        return toRankVOList(scores);
    }

    public void refreshShop(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() == null || shop.getStatus() != 1) {
            redisTemplate.opsForZSet().remove(REDIS_RANK_KEY, String.valueOf(shopId));
            return;
        }
        MerchantAccount merchant = merchantMapper.selectById(shop.getMerchantId());
        if (merchant == null || merchant.getAuditStatus() != MerchantAuditStatus.APPROVED.getCode()) {
            redisTemplate.opsForZSet().remove(REDIS_RANK_KEY, String.valueOf(shopId));
            return;
        }

        RankConfigVO config = getConfig();
        long sales = countSalesForShop(shopId);
        long reviews = countReviewsForShop(shopId);
        long violations = countViolationsForShop(shopId);
        BigDecimal weight = config.getSaleWeight().multiply(BigDecimal.valueOf(sales))
                .add(config.getReviewWeight().multiply(BigDecimal.valueOf(reviews)))
                .subtract(config.getViolationPenalty().multiply(BigDecimal.valueOf(violations)))
                .setScale(4, RoundingMode.HALF_UP);
        if (weight.compareTo(BigDecimal.ZERO) < 0) {
            weight = BigDecimal.ZERO;
        }
        shop.setWeightScore(weight);
        shopMapper.updateById(shop);
        if (weight.compareTo(BigDecimal.ZERO) > 0) {
            redisTemplate.opsForZSet().add(REDIS_RANK_KEY, String.valueOf(shopId), weight.doubleValue());
        } else {
            redisTemplate.opsForZSet().remove(REDIS_RANK_KEY, String.valueOf(shopId));
        }
    }

    private List<ShopRankVO> listFromDbFallback(int limit) {
        List<Shop> shops = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getStatus, 1)
                        .gt(Shop::getWeightScore, BigDecimal.ZERO)
                        .orderByDesc(Shop::getWeightScore)
                        .last("LIMIT " + limit)
        );
        List<ShopRankVO> result = new ArrayList<>();
        int rank = 1;
        for (Shop shop : shops) {
            result.add(ShopRankVO.builder()
                    .rank(rank++)
                    .shopId(shop.getId())
                    .shopName(shop.getName())
                    .logoUrl(shop.getLogoUrl())
                    .description(shop.getDescription())
                    .weightScore(shop.getWeightScore())
                    .build());
        }
        return result;
    }

    private List<ShopRankVO> buildRankList(List<Long> shopIds, Map<Long, Double> scoreMap) {
        if (shopIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Shop> shopMap = shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, s -> s));
        List<ShopRankVO> result = new ArrayList<>();
        int rank = 1;
        for (Long shopId : shopIds) {
            Shop shop = shopMap.get(shopId);
            if (shop == null) {
                continue;
            }
            result.add(ShopRankVO.builder()
                    .rank(rank++)
                    .shopId(shop.getId())
                    .shopName(shop.getName())
                    .logoUrl(shop.getLogoUrl())
                    .description(shop.getDescription())
                    .weightScore(BigDecimal.valueOf(scoreMap.getOrDefault(shopId, 0D)).setScale(4, RoundingMode.HALF_UP))
                    .build());
        }
        return result;
    }

    private List<ShopRankVO> toRankVOList(List<ShopScore> scores) {
        List<ShopRankVO> result = new ArrayList<>();
        int rank = 1;
        for (ShopScore item : scores) {
            result.add(ShopRankVO.builder()
                    .rank(rank++)
                    .shopId(item.shop.getId())
                    .shopName(item.shop.getName())
                    .logoUrl(item.shop.getLogoUrl())
                    .description(item.shop.getDescription())
                    .weightScore(item.weight)
                    .saleCount(item.sales)
                    .reviewCount(item.reviews)
                    .violationCount(item.violations)
                    .build());
        }
        return result;
    }

    private void saveSnapshot(List<ShopScore> scores) {
        LocalDate today = LocalDate.now();
        snapshotMapper.delete(new LambdaQueryWrapper<ShopRankSnapshot>()
                .eq(ShopRankSnapshot::getSnapshotDate, today));
        int position = 1;
        for (ShopScore item : scores) {
            ShopRankSnapshot snapshot = new ShopRankSnapshot();
            snapshot.setShopId(item.shop.getId());
            snapshot.setRankScore(item.weight);
            snapshot.setRankPosition(position++);
            snapshot.setSnapshotDate(today);
            snapshotMapper.insert(snapshot);
        }
    }

    private List<Shop> listActiveShops() {
        List<Shop> shops = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 1)
        );
        Set<Long> approvedMerchantIds = merchantMapper.selectList(
                new LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getAuditStatus, MerchantAuditStatus.APPROVED.getCode())
        ).stream().map(MerchantAccount::getId).collect(Collectors.toSet());

        List<Shop> active = new ArrayList<>();
        for (Shop shop : shops) {
            if (approvedMerchantIds.contains(shop.getMerchantId())) {
                active.add(shop);
            }
        }
        return active;
    }

    private Map<Long, Long> countSalesByShop() {
        Map<Long, Long> map = new HashMap<>();
        List<UserOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<UserOrder>()
                        .in(UserOrder::getStatus,
                                OrderStatus.PAID.getCode(),
                                OrderStatus.SHIPPED.getCode(),
                                OrderStatus.COMPLETED.getCode())
        );
        for (UserOrder order : orders) {
            map.merge(order.getShopId(), 1L, Long::sum);
        }
        return map;
    }

    private long countSalesForShop(Long shopId) {
        return orderMapper.selectCount(
                new LambdaQueryWrapper<UserOrder>()
                        .eq(UserOrder::getShopId, shopId)
                        .in(UserOrder::getStatus,
                                OrderStatus.PAID.getCode(),
                                OrderStatus.SHIPPED.getCode(),
                                OrderStatus.COMPLETED.getCode())
        );
    }

    private Map<Long, Long> countReviewsByShop() {
        Map<Long, Long> map = new HashMap<>();
        reviewMapper.selectList(null).forEach(r -> map.merge(r.getShopId(), 1L, Long::sum));
        return map;
    }

    private long countReviewsForShop(Long shopId) {
        return reviewMapper.selectCount(
                new LambdaQueryWrapper<com.market.entity.ProductReview>()
                        .eq(com.market.entity.ProductReview::getShopId, shopId)
        );
    }

    private Map<Long, Long> countViolationsByShop() {
        Map<Long, Long> map = new HashMap<>();
        productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getViolationFlag, 1)
        ).forEach(p -> map.merge(p.getShopId(), 1L, Long::sum));
        return map;
    }

    private long countViolationsForShop(Long shopId) {
        return productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getShopId, shopId)
                        .eq(Product::getViolationFlag, 1)
        );
    }

    private BigDecimal getConfigDecimal(String key, BigDecimal defaultValue) {
        PlatformConfig config = configMapper.selectById(key);
        if (config == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(config.getConfigValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void upsertConfig(String key, String value) {
        PlatformConfig config = configMapper.selectById(key);
        if (config == null) {
            config = new PlatformConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            configMapper.insert(config);
        } else {
            config.setConfigValue(value);
            configMapper.updateById(config);
        }
    }

    private static class ShopScore {
        private final Shop shop;
        private final BigDecimal weight;
        private final long sales;
        private final long reviews;
        private final long violations;

        ShopScore(Shop shop, BigDecimal weight, long sales, long reviews, long violations) {
            this.shop = shop;
            this.weight = weight;
            this.sales = sales;
            this.reviews = reviews;
            this.violations = violations;
        }

        BigDecimal getWeight() {
            return weight;
        }
    }
}
