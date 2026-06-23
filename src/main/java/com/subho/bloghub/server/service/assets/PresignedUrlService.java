package com.subho.bloghub.server.service.assets;

import com.subho.bloghub.server.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

/**
 * VLN-16b FIX: Centralised presigned-URL generation for reading S3 objects.
 *
 * The database stores only the S3 object key (e.g. "avatar/2025-01-01/uuid.jpg").
 * This service converts a key to a fresh presigned URL on every response, so URLs
 * never expire in storage and the bucket stays private.
 *
 * Callers should invoke {@link #toUrl(String)} before serialising any field that
 * holds an S3 key to the client.
 */
@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    /**
     * Returns a presigned GET URL for the given S3 key, valid for
     * {@code s3Properties.presignedUrlTtlMinutes} minutes.
     *
     * Returns {@code null} if the key is null or blank (handles optional image fields).
     *
     * If the value already starts with "https://" (e.g. an Unsplash stock photo
     * stored directly), it is returned as-is — only S3 keys need signing.
     */
    public String toUrl(String keyOrUrl) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) return null;
        if (keyOrUrl.startsWith("https://")) return keyOrUrl; // already a URL (e.g. stock image)

        Duration ttl = Duration.ofMinutes(s3Properties.getPresignedUrlTtlMinutes());
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(ttl)
                        .getObjectRequest(r -> r
                                .bucket(s3Properties.getBucketName())
                                .key(keyOrUrl)
                                .build())
                        .build());
        return presigned.url().toExternalForm();
    }
}
