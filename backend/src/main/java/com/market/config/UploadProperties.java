package com.market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Component
@ConfigurationProperties(prefix = "market.upload")
public class UploadProperties {

    private String dir = "uploads";
    private String baseUrl = "/uploads";
    private long maxSizeBytes = 2 * 1024 * 1024;

    public Path resolveDir() {
        Path path = Paths.get(dir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path.normalize();
    }
}
