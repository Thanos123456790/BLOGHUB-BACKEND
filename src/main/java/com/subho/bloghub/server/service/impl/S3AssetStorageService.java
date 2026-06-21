package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.assets.AssetUploadResponseDTO;
import com.subho.bloghub.client.enums.AssetType;
import com.subho.bloghub.server.config.S3Properties;
import com.subho.bloghub.server.exception.FileStorageException;
import com.subho.bloghub.server.service.assets.AssetStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * S3-backed implementation of {@link AssetStorageService}.
 *
 * Security measures applied before any byte reaches S3:
 *  - rejects empty files
 *  - enforces a hard size cap (defense in depth alongside Spring's
 *    multipart max-file-size, since that alone can be misconfigured)
 *  - validates the declared content type against an explicit allow-list
 *    (images only — this endpoint must never become a generic file host)
 *  - sniffs the actual file bytes' magic numbers rather than trusting the
 *    client-supplied Content-Type header, which is trivial to spoof
 *  - generates a random, non-guessable object key server-side — the
 *    client's original filename is never used as or embedded in the S3
 *    key, which avoids path traversal and key-collision/overwrite attacks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3AssetStorageService implements AssetStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @Autowired
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public AssetUploadResponseDTO upload(MultipartFile file, AssetType type) {
        validate(file);

        String contentType = sniffContentType(file);
        String extension = extensionFor(contentType);
        String key = buildKey(type, extension);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.getBucketName())
                            .key(key)
                            .contentType(contentType)
                            .contentLength(file.getSize())
                            // Private by default — callers get back a URL but the
                            // bucket itself should not be publicly writable/listable.
                            // Serve via CloudFront/presigned URL in production.
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            log.error("Failed to read uploaded file for key {}", key, e);
            throw new FileStorageException("Could not read uploaded file");
        } catch (S3Exception e) {
            log.error("S3 upload failed for key {}", key, e);
            throw new FileStorageException(HttpStatus.BAD_GATEWAY, "Upload to storage failed");
        }

        return AssetUploadResponseDTO.builder()
                .url(publicUrl(key))
                .key(key)
                .contentType(contentType)
                .sizeBytes(file.getSize())
                .build();
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            // Deletion failures shouldn't block the user-facing flow (e.g.
            // replacing an avatar) — log for cleanup/monitoring instead.
            log.warn("Failed to delete S3 object {}", key, e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Uploaded file is empty");
        }
        if (file.getSize() > s3Properties.getMaxFileSizeBytes()) {
            throw new FileStorageException("File exceeds the maximum allowed size of "
                    + (s3Properties.getMaxFileSizeBytes() / (1024 * 1024)) + "MB");
        }
        String declaredType = file.getContentType();
        if (declaredType == null || !ALLOWED_TYPES.contains(declaredType.toLowerCase())) {
            throw new FileStorageException("Unsupported file type. Allowed types: " + ALLOWED_TYPES);
        }
    }

    /**
     * Reads the first bytes of the file to confirm its real type via magic
     * number, instead of trusting the client-supplied Content-Type header.
     * Falls back to rejecting the upload if the bytes don't match a known
     * image signature, even if the header claimed an allowed type.
     */
    private String sniffContentType(MultipartFile file) {
        byte[] header;
        try {
            header = file.getInputStream().readNBytes(12);
        } catch (IOException e) {
            throw new FileStorageException("Could not read uploaded file");
        }

        if (matches(header, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (matches(header, 0x89, 0x50, 0x4E, 0x47)) {
            return "image/png";
        }
        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "image/webp";
        }
        if (header.length >= 6
                && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8') {
            return "image/gif";
        }

        throw new FileStorageException("File content does not match a supported image format");
    }

    private boolean matches(byte[] header, int... signature) {
        if (header.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((header[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "bin";
        };
    }

    /**
     * Server-generated key — never derived from client input. Namespaced by
     * asset type and upload date for easy lifecycle-policy management
     * (e.g. expiring orphaned uploads) and bucket browsing.
     */
    private String buildKey(AssetType type, String extension) {
        String prefix = type.name().toLowerCase();
        String datePath = Instant.now().toString().substring(0, 10); // yyyy-MM-dd
        return "%s/%s/%s.%s".formatted(prefix, datePath, UUID.randomUUID(), extension);
    }

    private String publicUrl(String key) {
        String endpoint = s3Properties.getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint.replaceAll("/+$", "") + "/" + s3Properties.getBucketName() + "/" + key;
        }
        return "https://%s.s3.amazonaws.com/%s".formatted(s3Properties.getBucketName(), key);
    }
}
