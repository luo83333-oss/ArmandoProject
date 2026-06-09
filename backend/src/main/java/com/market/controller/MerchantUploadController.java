package com.market.controller;

import com.market.common.Result;
import com.market.dto.upload.UploadResultVO;
import com.market.security.UserContext;
import com.market.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/merchant/upload")
@RequiredArgsConstructor
public class MerchantUploadController {

    private final UploadService uploadService;

    @PostMapping
    public Result<UploadResultVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(uploadService.uploadImage(UserContext.getUserId(), file));
    }
}
