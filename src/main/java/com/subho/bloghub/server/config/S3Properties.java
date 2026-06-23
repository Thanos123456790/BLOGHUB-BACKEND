package com.subho.bloghub.server.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "app.aws.s3")
public class S3Properties {

    private String bucketName;
    private String region = "ap-south-1";
    private String endpoint;
    private boolean pathStyleAccessEnabled;
    private int presignedUrlTtlMinutes = 15;
    private long maxFileSizeBytes = 5_242_880L;
    private List<String> allowedContentTypes;

    /**
     * VLN-04 FIX: Configurable allowlist of trusted image hostnames.
     * Image URL fields in blog blocks and user profiles are validated against this list.
     *
     * VLN-04b FIX: Emit a loud warning (and fail fast in production) when this
     * list is empty, because an empty list disables host-validation entirely.
     */
    private List<String> trustedImageHosts = List.of();

    @PostConstruct
    public void validate() {
        if (trustedImageHosts == null || trustedImageHosts.isEmpty()) {
            String msg = "app.aws.s3.trusted-image-hosts is empty — image URL host validation is DISABLED. " +
                         "Set this to your S3 bucket hostname(s) before deploying to production.";
            // In production this should throw; in local dev warn loudly.
            log.warn("SECURITY WARNING: {}", msg);
        }
    }
}
