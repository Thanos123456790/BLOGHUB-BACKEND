package com.subho.bloghub.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Activates {@link RateLimitProperties} binding from {@code app.rate-limit.*}.
 * The actual enforcement is done by {@link RateLimitFilter} which is
 * auto-registered as a Spring component.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {
}
