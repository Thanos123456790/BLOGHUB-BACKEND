package com.subho.bloghub.client.dtos.assets;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of a successful asset upload to S3")
public class AssetUploadResponseDTO {

    @Schema(description = "Publicly resolvable URL of the uploaded asset (or the object key, if private)")
    private String url;

    @Schema(description = "S3 object key — useful if the caller later needs to delete/replace this asset")
    private String key;

    @Schema(description = "Uploaded content type", example = "image/jpeg")
    private String contentType;

    @Schema(description = "Uploaded file size in bytes")
    private long sizeBytes;
}
