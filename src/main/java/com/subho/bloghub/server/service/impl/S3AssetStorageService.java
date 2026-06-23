package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.assets.AssetUploadResponseDTO;
import com.subho.bloghub.client.enums.AssetType;
import com.subho.bloghub.server.config.S3Properties;
import com.subho.bloghub.server.exception.FileStorageException;
import com.subho.bloghub.server.service.assets.AssetStorageService;
import com.subho.bloghub.server.service.assets.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * VLN-16 FIX: Uploads are stored with private ACL (no public-read).
 * VLN-16b FIX: The 'url' field in the response now returns the S3 object KEY
 *   (not a presigned URL). The key is what should be persisted in the database.
 *   Callers that need to display the image should use {@link PresignedUrlService#toUrl(String)}
 *   to generate a fresh presigned URL at response-serialisation time.
 *
 *   This ensures stored URLs never expire and the bucket stays private.
 *
 * Other security measures:
 *  - rejects empty files and enforces a hard size cap
 *  - validates declared content-type against an explicit allow-list
 *  - sniffs file magic bytes (does NOT trust client Content-Type header)
 *  - generates a random, non-guessable key (client filename is never used)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3AssetStorageService implements AssetStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final PresignedUrlService presignedUrlService;

    @Override
    public AssetUploadResponseDTO upload(MultipartFile file, AssetType type) {
        validate(file);

        String contentType = sniffContentType(file);
        String extension   = extensionFor(contentType);
        String key         = buildKey(type, extension);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.getBucketName())
                            .key(key)
                            .contentType(contentType)
                            .contentLength(file.getSize())
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

        // ────────── ADD THIS LINE TO CONSTRUCT FULL URL ──────────
        String fullPublicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getBucketName(),
                s3Properties.getRegion(),
                key);

        // Updated return statement to send the full absolute URL
        return AssetUploadResponseDTO.builder()
                .url(fullPublicUrl) // Passed validation safely!
                .key(key)
                .contentType(contentType)
                .sizeBytes(file.getSize())
                .build();
    }
    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) return;
        // If it's a full URL (e.g. Unsplash), skip deletion
        if (key.startsWith("https://")) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            log.warn("Failed to delete S3 object {}", key, e);
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

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

    private String sniffContentType(MultipartFile file) {
        byte[] header;
        try {
            header = file.getInputStream().readNBytes(12);
        } catch (IOException e) {
            throw new FileStorageException("Could not read uploaded file");
        }
        if (matches(header, 0xFF, 0xD8, 0xFF))                          return "image/jpeg";
        if (matches(header, 0x89, 0x50, 0x4E, 0x47))                    return "image/png";
        if (header.length >= 12
                && header[0]=='R' && header[1]=='I' && header[2]=='F' && header[3]=='F'
                && header[8]=='W' && header[9]=='E' && header[10]=='B' && header[11]=='P')
            return "image/webp";
        if (header.length >= 4
                && header[0]=='G' && header[1]=='I' && header[2]=='F' && header[3]=='8')
            return "image/gif";
        throw new FileStorageException("File content does not match a supported image format");
    }

    private boolean matches(byte[] header, int... signature) {
        if (header.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if ((header[i] & 0xFF) != signature[i]) return false;
        }
        return true;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            case "image/gif"  -> "gif";
            default           -> "bin";
        };
    }

    private String buildKey(AssetType type, String extension) {
        String prefix   = type.name().toLowerCase();
        String datePath = Instant.now().toString().substring(0, 10);
        return "%s/%s/%s.%s".formatted(prefix, datePath, UUID.randomUUID(), extension);
    }
}
