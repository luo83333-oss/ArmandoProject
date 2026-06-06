package com.market.controller;

import com.market.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("app", "ok");

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            status.put("mysql", "ok");
        } catch (Exception ex) {
            status.put("mysql", "error");
        }

        try {
            redisTemplate.opsForValue().set("market:health:ping", "pong");
            status.put("redis", "ok");
        } catch (Exception ex) {
            status.put("redis", "error");
        }

        return Result.ok(status);
    }
}
