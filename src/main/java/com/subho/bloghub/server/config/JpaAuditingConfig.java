package com.subho.bloghub.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Without this, {@code @CreatedDate}/{@code @LastModifiedDate} on
 * {@code BaseEntity} are silently ignored and every entity falls back to
 * its manual {@code @PrePersist}/{@code @PreUpdate} hooks only.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
