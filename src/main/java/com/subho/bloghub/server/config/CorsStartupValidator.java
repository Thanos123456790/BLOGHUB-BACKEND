package com.subho.bloghub.server.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * VLN-18 FIX: Guards against localhost CORS origins leaking into production.
 *
 * If the app is started with the 'prod' Spring profile and the allowed-origins
 * value still contains 'localhost' or '127.0.0.1', the application will refuse
 * to start with a clear error message.
 *
 * This runs on 'prod' profile only so it never blocks local development.
 */
@Slf4j
@Component
@Profile("prod")
public class CorsStartupValidator {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    @PostConstruct
    public void validate() {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            throw new IllegalStateException(
                    "SECURITY: app.cors.allowed-origins is not set. " +
                    "Set CORS_ALLOWED_ORIGINS to your production frontend URL before deploying.");
        }
        if (allowedOrigins.contains("localhost") || allowedOrigins.contains("127.0.0.1")) {
            throw new IllegalStateException(
                    "SECURITY: app.cors.allowed-origins contains a localhost value (" + allowedOrigins + "). " +
                    "This is not allowed in the 'prod' profile. " +
                    "Set CORS_ALLOWED_ORIGINS to your production frontend URL.");
        }
        log.info("CORS allowed origins validated: {}", allowedOrigins);
    }
}
