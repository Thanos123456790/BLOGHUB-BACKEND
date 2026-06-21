package com.subho.bloghub.client.dtos.reaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Breakdown of reaction counts for a blog or comment")
public class ReactionCountResponseDTO {

    @Schema(description = "Number of 👍 Like reactions", example = "412")
    private long like;

    @Schema(description = "Number of 👏 Clap reactions", example = "198")
    private long clap;

    @Schema(description = "Number of ❤️ Love reactions", example = "0")
    private long love;

    @Schema(description = "Number of 💡 Insightful reactions", example = "86")
    private long insightful;

    @Schema(description = "Total reaction count across all types", example = "696")
    public long getTotal() {
        return like + clap + love + insightful;
    }
}

