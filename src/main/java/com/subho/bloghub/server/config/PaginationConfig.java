package com.subho.bloghub.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.subho.bloghub.server.common.PageRequestFactory;

@Configuration
@EnableConfigurationProperties(PaginationProperties.class)
public class PaginationConfig {

    @Bean
    public PageRequestFactory pageRequestFactory(PaginationProperties properties) {
        return new PageRequestFactory(properties);
    }
}
