package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.OrderStatus;
import com.market.dto.platform.PlatformShopStatsVO;
import com.market.dto.platform.PlatformStatsOverviewVO;
import com.market.dto.platform.PlatformStatsTrendPointVO;
import com.market.entity.Shop;
import com.market.entity.UserOrder;
import com.market.mapper.ShopMapper;
import com.market.mapper.UserOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformStatsService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final List<Integer> PAID_STATUSES = List.of(
            OrderStatus.PAID.getCode(),
            OrderStatus.SHIPPED.getCode(),
            OrderStatus.COMPLETED.getCode()
    );

    private final UserOrderMapper orderMapper;
    private final ShopMapper shopMapper;

    public PlatformStatsOverviewVO getOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        List<UserOrder> paidOrders = orderMapper.selectList(
                new LambdaQueryWrapper<UserOrder>().in(UserOrder::getStatus, PAID_STATUSES)
        );

        BigDecimal todayGmv = BigDecimal.ZERO;
        long todayCount = 0;
        BigDecimal totalGmv = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;

        for (UserOrder order : paidOrders) {
            BigDecimal pay = amount(order.getPayAmount());
            BigDecimal commission = amount(order.getCommissionAmount());
            totalGmv = totalGmv.add(pay);
            totalCommission = totalCommission.add(commission);
            if (order.getPaidAt() != null
                    && !order.getPaidAt().isBefore(todayStart)
                    && order.getPaidAt().isBefore(todayEnd)) {
                todayGmv = todayGmv.add(pay);
                todayCount++;
            }
        }

        long activeShops = paidOrders.stream()
                .map(UserOrder::getShopId)
                .distinct()
                .count();

        return PlatformStatsOverviewVO.builder()
                .todayGmv(todayGmv)
                .todayOrderCount(todayCount)
                .totalGmv(totalGmv)
                .totalOrderCount(paidOrders.size())
                .totalCommission(totalCommission)
                .activeShopCount(activeShops)
                .build();
    }

    public List<PlatformStatsTrendPointVO> getTrend(int days, String granularity) {
        int range = Math.min(Math.max(days, 7), 365);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(range - 1L);
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.plusDays(1).atStartOfDay();

        List<UserOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<UserOrder>()
                        .in(UserOrder::getStatus, PAID_STATUSES)
                        .ge(UserOrder::getPaidAt, rangeStart)
                        .lt(UserOrder::getPaidAt, rangeEnd)
        );

        String mode = granularity == null ? "day" : granularity.toLowerCase();
        Map<String, BigDecimal> gmvMap = new HashMap<>();
        Map<String, Long> countMap = new HashMap<>();
        Map<String, BigDecimal> commissionMap = new HashMap<>();

        for (UserOrder order : orders) {
            if (order.getPaidAt() == null) {
                continue;
            }
            String period = periodKey(order.getPaidAt().toLocalDate(), mode);
            gmvMap.merge(period, amount(order.getPayAmount()), BigDecimal::add);
            countMap.merge(period, 1L, Long::sum);
            commissionMap.merge(period, amount(order.getCommissionAmount()), BigDecimal::add);
        }

        List<String> periods = buildPeriods(start, end, mode);
        List<PlatformStatsTrendPointVO> points = new ArrayList<>();
        for (String period : periods) {
            points.add(PlatformStatsTrendPointVO.builder()
                    .period(period)
                    .gmvAmount(gmvMap.getOrDefault(period, BigDecimal.ZERO))
                    .orderCount(countMap.getOrDefault(period, 0L))
                    .commissionAmount(commissionMap.getOrDefault(period, BigDecimal.ZERO))
                    .build());
        }
        return points;
    }

    public List<PlatformShopStatsVO> getShopRanking(int days, int limit) {
        int range = Math.min(Math.max(days, 7), 365);
        int size = Math.min(Math.max(limit, 1), 100);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(range - 1L);

        List<UserOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<UserOrder>()
                        .in(UserOrder::getStatus, PAID_STATUSES)
                        .ge(UserOrder::getPaidAt, start.atStartOfDay())
                        .lt(UserOrder::getPaidAt, end.plusDays(1).atStartOfDay())
        );

        Map<Long, BigDecimal> gmvByShop = new HashMap<>();
        Map<Long, Long> countByShop = new HashMap<>();
        Map<Long, BigDecimal> commissionByShop = new HashMap<>();

        for (UserOrder order : orders) {
            Long shopId = order.getShopId();
            gmvByShop.merge(shopId, amount(order.getPayAmount()), BigDecimal::add);
            countByShop.merge(shopId, 1L, Long::sum);
            commissionByShop.merge(shopId, amount(order.getCommissionAmount()), BigDecimal::add);
        }

        Map<Long, String> shopNames = shopMapper.selectBatchIds(gmvByShop.keySet()).stream()
                .collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));

        List<PlatformShopStatsVO> ranking = gmvByShop.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(size)
                .map(entry -> PlatformShopStatsVO.builder()
                        .shopId(entry.getKey())
                        .shopName(shopNames.getOrDefault(entry.getKey(), "店铺#" + entry.getKey()))
                        .gmvAmount(entry.getValue())
                        .orderCount(countByShop.getOrDefault(entry.getKey(), 0L))
                        .commissionAmount(commissionByShop.getOrDefault(entry.getKey(), BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }
        return ranking;
    }

    public String exportShopRankingCsv(int days) {
        List<PlatformShopStatsVO> list = getShopRanking(days, 100);
        StringBuilder sb = new StringBuilder();
        sb.append("rank,shopId,shopName,gmvAmount,orderCount,commissionAmount\n");
        for (PlatformShopStatsVO row : list) {
            sb.append(row.getRank()).append(',')
                    .append(row.getShopId()).append(',')
                    .append(escapeCsv(row.getShopName())).append(',')
                    .append(row.getGmvAmount()).append(',')
                    .append(row.getOrderCount()).append(',')
                    .append(row.getCommissionAmount()).append('\n');
        }
        return sb.toString();
    }

    private BigDecimal amount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String periodKey(LocalDate date, String mode) {
        if ("week".equals(mode)) {
            LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return weekStart.toString();
        }
        if ("month".equals(mode)) {
            return YearMonth.from(date).format(MONTH_FMT);
        }
        return date.toString();
    }

    private List<String> buildPeriods(LocalDate start, LocalDate end, String mode) {
        List<String> periods = new ArrayList<>();
        if ("week".equals(mode)) {
            LocalDate cursor = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endWeek = end.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            while (!cursor.isAfter(endWeek)) {
                periods.add(cursor.toString());
                cursor = cursor.plusWeeks(1);
            }
            return periods;
        }
        if ("month".equals(mode)) {
            YearMonth cursor = YearMonth.from(start);
            YearMonth endMonth = YearMonth.from(end);
            while (!cursor.isAfter(endMonth)) {
                periods.add(cursor.format(MONTH_FMT));
                cursor = cursor.plusMonths(1);
            }
            return periods;
        }
        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            periods.add(cursor.toString());
        }
        return periods;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
