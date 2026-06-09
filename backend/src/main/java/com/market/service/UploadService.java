package com.market.service;

import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.config.UploadProperties;
import com.market.dto.upload.UploadResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/pjpeg", "image/png", "image/webp",
            "image/x-png", "application/octet-stream"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final UploadProperties uploadProperties;
    private final MerchantShopService merchantShopService;

    public UploadResultVO uploadImage(Long userId, MultipartFile file) {
        merchantShopService.requireApprovedShop(userId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请选择图片文件");
        }
        if (file.getSize() > uploadProperties.getMaxSizeBytes()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "图片大小不能超过 2MB");
        }
        String ext = resolveExtension(file);
        if (ext == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "仅支持 JPEG、PNG、WebP 格式");
        }
        LocalDate today = LocalDate.now();
        Path targetDir = uploadProperties.resolveDir()
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()));
        try {
            Files.createDirectories(targetDir);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = targetDir.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String url = uploadProperties.getBaseUrl() + "/" + today.getYear()
                    + "/" + String.format("%02d", today.getMonthValue()) + "/" + filename;
            return new UploadResultVO(url);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "图片保存失败");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType)) {
            String type = contentType.toLowerCase(Locale.ROOT);
            if (ALLOWED_TYPES.contains(type)) {
                if (type.contains("png")) {
                    return ".png";
                }
                if (type.contains("webp")) {
                    return ".webp";
                }
                return ".jpg";
            }
        }
        String ext = extensionFromFilename(file.getOriginalFilename());
        if (ext != null && ALLOWED_EXTENSIONS.contains(ext)) {
            return ext;
        }
        return null;
    }

    private String extensionFromFilename(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
}
