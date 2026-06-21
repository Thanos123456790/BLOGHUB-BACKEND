package com.subho.bloghub.client.enums;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported reaction types for blogs and comments")
public enum ReactionType {
    @Schema(description = "👍 Like reaction")
    LIKE,
    @Schema(description = "👏 Clap reaction")
    CLAP,
    @Schema(description = "❤️ Love reaction")
    LOVE,
    @Schema(description = "💡 Insightful reaction")
    INSIGHTFUL
}
