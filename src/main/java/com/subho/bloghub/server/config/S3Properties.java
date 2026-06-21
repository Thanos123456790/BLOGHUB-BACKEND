package com.subho.bloghub.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Binds the {@code app.aws.s3} section of application config. Credentials
 * are intentionally NOT bound here — they're resolved via the AWS SDK's
 * default credential provider chain (env vars, shared config/profile, or
 * instance/task role), never read out of application.yaml. See
 * {@link S3Config}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.aws.s3")
public class S3Properties {

    /** Target bucket for all asset uploads. */
    private String bucketName;

    /** Optional custom endpoint (e.g. LocalStack). Blank = real AWS S3. */
    private String endpoint;

    /** Required for LocalStack/MinIO style endpoints; false for real S3. */
    private boolean pathStyleAccessEnabled;

    /** How long a generated presigned GET URL stays valid. */
    private int presignedUrlTtlMinutes = 15;

    /** Hard cap on upload size, enforced server-side before any S3 call. */
    private long maxFileSizeBytes = 5_242_880L; // 5MB

    /** Whitelist of content types accepted for upload. */
    private List<String> allowedContentTypes;
}
