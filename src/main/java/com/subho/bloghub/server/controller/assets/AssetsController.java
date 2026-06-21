package com.subho.bloghub.server.controller.assets;

import com.subho.bloghub.client.apis.assets.AssetsAPI;
import com.subho.bloghub.client.dtos.assets.AssetUploadResponseDTO;
import com.subho.bloghub.client.enums.AssetType;
import com.subho.bloghub.server.service.assets.AssetStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AssetsController implements AssetsAPI {

    private final AssetStorageService assetStorageService;

    @Override
    public ResponseEntity<AssetUploadResponseDTO> uploadAsset(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") AssetType type) {
        return ResponseEntity.ok(assetStorageService.upload(file, type));
    }
}
