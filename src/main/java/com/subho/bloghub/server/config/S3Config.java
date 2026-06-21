package com.subho.bloghub.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Wires the AWS S3 client/presigner used for all asset uploads (avatars,
 * banners, blog covers, blog block images).
 *
 * Security note: credentials are resolved exclusively through
 * {@link DefaultCredentialsProvider}, which checks (in order) environment
 * variables, system properties, the shared AWS config/credentials file,
 * and finally the container/instance role. Nothing here ever reads a
 * secret key out of application config — that was the original bug in
 * application-local.yaml and is intentionally not repeated.
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Value("${app.aws.region:ap-south-1}")
    private String region;

    @Bean
    public S3Client s3Client(S3Properties props) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(props.isPathStyleAccessEnabled())
                        .build());

        if (StringUtils.hasText(props.getEndpoint())) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (StringUtils.hasText(props.getEndpoint())) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        return builder.build();
    }
}
