package com.subho.bloghub.client.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic paginated response wrapper")
public class PageResponseDTO<T> {

    @Schema(description = "Page content items")
    private List<T> content;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Items per page", example = "10")
    private int size;

    @Schema(description = "Total matching items", example = "154")
    private long totalElements;

    @Schema(description = "Total pages", example = "16")
    private int totalPages;

    @Schema(description = "Whether this is the last page")
    private boolean last;

    @Schema(description = "Whether this is the first page")
    private boolean first;
}

