package com.subho.bloghub.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable rate-limit thresholds.
 * Bound from the {@code app.rate-limit.*} namespace in application.yaml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /** Max asset uploads per user per hour. */
    private int assetUploadPerUserPerHour = 20;

    /** Max write operations (create/update/delete blog, comment, react) per user per minute. */
    private int writePerUserPerMinute = 30;
}
