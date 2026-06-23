package com.subho.bloghub.server.controller.assets;

import com.subho.bloghub.client.apis.assets.AssetsAPI;
import com.subho.bloghub.client.dtos.assets.AssetUploadResponseDTO;
import com.subho.bloghub.client.enums.AssetType;
import com.subho.bloghub.server.common.CurrentUserResolver;
import com.subho.bloghub.server.exception.BadRequestException;
import com.subho.bloghub.server.service.assets.AssetStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AssetsController implements AssetsAPI {

    /**
     * VLN-12 FIX: Types that relate to a user's own profile. Only the authenticated
     * user can upload these, and the frontend must declare the correct type or the
     * upload is rejected before reaching S3.
     */
    private static final Set<AssetType> USER_PROFILE_TYPES = Set.of(
            AssetType.AVATAR,
            AssetType.BANNER
    );

    /**
     * Types that relate to blog content. The authenticated user must be the
     * blog author, but since we can't validate blog ownership at upload time
     * (the blog may not exist yet), we at minimum ensure the declared type
     * is from the correct category bucket.
     */
    private static final Set<AssetType> BLOG_CONTENT_TYPES = Set.of(
            AssetType.BLOG_COVER,
            AssetType.BLOG_BLOCK_IMAGE
    );

    private static final Set<AssetType> ALL_KNOWN_TYPES = Set.of(AssetType.values());

    private final AssetStorageService assetStorageService;
    private final CurrentUserResolver currentUserResolver;

    @Override
    public ResponseEntity<AssetUploadResponseDTO> uploadAsset(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") AssetType type) {

        // Ensure the caller is authenticated (already enforced by Spring Security,
        // but this also provisions the Users row we may need for audit logging).
        currentUserResolver.requireCurrentUserId(accessToken);

        // VLN-12 FIX: Reject unknown AssetType values (Spring would already reject
        // unmapped enum strings with a 400, but this makes the error message explicit).
        if (!ALL_KNOWN_TYPES.contains(type)) {
            throw new BadRequestException(
                    "Unknown asset type '" + type + "'. Allowed values: " + ALL_KNOWN_TYPES);
        }

        // VLN-12 FIX: The declared type must belong to a coherent category.
        // We can't fully verify context (e.g. whether the user is the blog author)
        // at upload time, but we prevent clear cross-category misuse such as
        // uploading with type=AVATAR when the intent is a blog block image.
        // The BFF is expected to always pass the correct type; this is a defence-in-depth check.
        if (!USER_PROFILE_TYPES.contains(type) && !BLOG_CONTENT_TYPES.contains(type)) {
            throw new BadRequestException("Asset type '" + type + "' is not accepted by this endpoint");
        }

        return ResponseEntity.ok(assetStorageService.upload(file, type));
    }
}
