package com.market.controller;

import com.market.common.Result;
import com.market.common.UserRole;
import com.market.dto.platform.PlatformUserVO;
import com.market.dto.platform.UpdateUserStatusRequest;
import com.market.security.RequireRole;
import com.market.security.UserContext;
import com.market.service.PlatformUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/platform/users")
@RequiredArgsConstructor
@Validated
public class PlatformUserController {

    private final PlatformUserService platformUserService;

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public Result<List<PlatformUserVO>> list() {
        return Result.ok(platformUserService.list());
    }

    @PatchMapping("/{id}/status")
    @RequireRole(UserRole.ADMIN)
    public Result<PlatformUserVO> updateStatus(@PathVariable Long id,
                                               @Valid @RequestBody UpdateUserStatusRequest request) {
        return Result.ok(platformUserService.updateStatus(
                UserContext.getUserId(), id, request.getStatus()));
    }
}
