package com.market.controller;

import com.market.common.Result;
import com.market.common.UserRole;
import com.market.dto.platform.PlatformShopStatsVO;
import com.market.dto.platform.PlatformStatsOverviewVO;
import com.market.dto.platform.PlatformStatsTrendPointVO;
import com.market.security.RequireRole;
import com.market.service.PlatformStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/platform/stats")
@RequiredArgsConstructor
public class PlatformStatsController {

    private final PlatformStatsService platformStatsService;

    @GetMapping("/overview")
    @RequireRole(UserRole.ADMIN)
    public Result<PlatformStatsOverviewVO> overview() {
        return Result.ok(platformStatsService.getOverview());
    }

    @GetMapping("/trend")
    @RequireRole(UserRole.ADMIN)
    public Result<List<PlatformStatsTrendPointVO>> trend(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "day") String granularity) {
        return Result.ok(platformStatsService.getTrend(days, granularity));
    }

    @GetMapping("/shops")
    @RequireRole(UserRole.ADMIN)
    public Result<List<PlatformShopStatsVO>> shopRanking(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(platformStatsService.getShopRanking(days, limit));
    }

    @GetMapping("/shops/export")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<byte[]> exportShopRanking(@RequestParam(defaultValue = "30") int days) {
        String csv = platformStatsService.exportShopRankingCsv(days);
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=shop-stats.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
