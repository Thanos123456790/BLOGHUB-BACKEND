package com.subho.bloghub.client.dtos.tags;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A tag with the number of blogs using it")
public class TagResponseDTO {

    @Schema(description = "Tag name", example = "Engineering")
    private String name;

    @Schema(description = "Number of blogs using this tag", example = "128")
    private long postCount;
}
