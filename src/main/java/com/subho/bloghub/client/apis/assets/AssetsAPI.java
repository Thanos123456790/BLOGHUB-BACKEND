package com.subho.bloghub.client.apis.assets;

import com.subho.bloghub.client.dtos.assets.AssetUploadResponseDTO;
import com.subho.bloghub.client.enums.AssetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Assets API", description = "Uploads images (avatars, banners, blog covers, blog block images) to S3")
@RequestMapping("/api/v1/assets")
public interface AssetsAPI {

    @Operation(
            summary = "Upload an image asset",
            description = "Uploads an image to S3 under a key namespaced by asset type and returns its URL. " +
                    "Validates content type and size server-side before ever calling S3."
    )
    @ApiResponse(responseCode = "200", description = "Asset uploaded successfully")
    @ApiResponse(responseCode = "400", description = "File missing, too large, or an unsupported content type")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    ResponseEntity<AssetUploadResponseDTO> uploadAsset(
            String accessToken,
            @Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Asset category, controls S3 key prefix") @RequestParam("type") AssetType type
    );
}
