package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.OrderStatus;
import com.market.dto.merchant.MerchantStatsOverviewVO;
import com.market.dto.merchant.MerchantStatsTrendPointVO;
import com.market.entity.Shop;
import com.market.entity.UserOrder;
import com.market.mapper.UserOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MerchantStatsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final List<Integer> PAID_STATUSES = List.of(
            OrderStatus.PAID.getCode(),
            OrderStatus.SHIPPED.getCode(),
            OrderStatus.COMPLETED.getCode()
    );

    private final MerchantShopService merchantShopService;
    private final UserOrderMapper orderMapper;

    public MerchantStatsOverviewVO getOverview(Long userId) {
        Shop shop = merchantShopService.requireApprovedShop(userId);
        Long shopId = shop.getId();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime recentStart = today.minusDays(6).atStartOfDay();

        List<UserOrder> paidOrders = orderMapper.selectList(
                new LambdaQueryWrapper<UserOrder>()
                        .eq(UserOrder::getShopId, shopId)
                        .in(UserOrder::getStatus, PAID_STATUSES)
        );

        BigDecimal todaySales = BigDecimal.ZERO;
        long todayCount = 0;
        BigDecimal totalSales = BigDecimal.ZERO;
        Set<Long> recentBuyers = new HashSet<>();

        for (UserOrder order : paidOrders) {
            BigDecimal amount = order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO;
            totalSales = totalSales.add(amount);
            if (order.getPaidAt() != null
                    && !order.getPaidAt().isBefore(todayStart)
                    && order.getPaidAt().isBefore(todayEnd)) {
                todaySales = todaySales.add(amount);
                todayCount++;
            }
            if (order.getPaidAt() != null
                    && !order.getPaidAt().isBefore(recentStart)
                    && order.getPaidAt().isBefore(todayEnd)) {
                recentBuyers.add(order.getUserId());
            }
        }

        long pendingShip = orderMapper.selectCount(
                new LambdaQueryWrapper<UserOrder>()
                        .eq(UserOrder::getShopId, shopId)
                        .eq(UserOrder::getStatus, OrderStatus.PAID.getCode())
        );

        return MerchantStatsOverviewVO.builder()
                .todaySalesAmount(todaySales)
                .todayOrderCount(todayCount)
                .totalSalesAmount(totalSales)
                .totalOrderCount(paidOrders.size())
                .pendingShipCount(pendingShip)
                .recentBuyerCount(recentBuyers.size())
                .build();
    }

    public List<MerchantStatsTrendPointVO> getTrend(Long userId, int days) {
        Shop shop = merchantShopService.requireApprovedShop(userId);
        int range = Math.min(Math.max(days, 7), 90);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(range - 1L);
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.plusDays(1).atStartOfDay();

        List<UserOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<UserOrder>()
                        .eq(UserOrder::getShopId, shop.getId())
                        .in(UserOrder::getStatus, PAID_STATUSES)
                        .ge(UserOrder::getPaidAt, rangeStart)
                        .lt(UserOrder::getPaidAt, rangeEnd)
        );

        Map<LocalDate, BigDecimal> salesByDate = new HashMap<>();
        Map<LocalDate, Long> ordersByDate = new HashMap<>();
        Map<LocalDate, Set<Long>> buyersByDate = new HashMap<>();

        for (UserOrder order : orders) {
            if (order.getPaidAt() == null) {
                continue;
            }
            LocalDate date = order.getPaidAt().toLocalDate();
            BigDecimal amount = order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO;
            salesByDate.merge(date, amount, BigDecimal::add);
            ordersByDate.merge(date, 1L, Long::sum);
            buyersByDate.computeIfAbsent(date, k -> new HashSet<>()).add(order.getUserId());
        }

        List<MerchantStatsTrendPointVO> points = new ArrayList<>();
        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            points.add(MerchantStatsTrendPointVO.builder()
                    .date(cursor.format(DATE_FMT))
                    .salesAmount(salesByDate.getOrDefault(cursor, BigDecimal.ZERO))
                    .orderCount(ordersByDate.getOrDefault(cursor, 0L))
                    .buyerCount((long) buyersByDate.getOrDefault(cursor, new HashSet<>()).size())
                    .build());
        }
        return points;
    }
}
