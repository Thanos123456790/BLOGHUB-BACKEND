package com.subho.bloghub.client.dtos.blogs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated blog feed response")
public class BlogFeedResponseDTO {

    @Schema(description = "Blog cards for this page")
    private List<BlogCardResponseDTO> blogs;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "10")
    private int size;

    @Schema(description = "Total number of matching blogs", example = "154")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "16")
    private int totalPages;

    @Schema(description = "Whether this is the last page")
    private boolean last;
}

