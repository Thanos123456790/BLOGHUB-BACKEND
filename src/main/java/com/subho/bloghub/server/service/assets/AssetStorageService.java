package com.subho.bloghub.server.service.assets;

import com.subho.bloghub.client.dtos.assets.AssetUploadResponseDTO;
import com.subho.bloghub.client.enums.AssetType;
import org.springframework.web.multipart.MultipartFile;

public interface AssetStorageService {

    /**
     * Validates and uploads a single image file, returning its public URL
     * and S3 key. Implementations must validate content type, size, and
     * file integrity BEFORE making any call to the storage backend.
     */
    AssetUploadResponseDTO upload(MultipartFile file, AssetType type);

    /**
     * Deletes a previously uploaded asset by its S3 key. Used when a user
     * replaces their avatar/banner or a blog cover, to avoid orphaned
     * objects accumulating in the bucket.
     */
    void delete(String key);
}
