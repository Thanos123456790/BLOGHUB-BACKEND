package com.subho.bloghub.client.dtos.reaction;

import com.subho.bloghub.client.enums.ReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to add or change a reaction on a blog or comment")
public class ReactionRequestDTO {

    @Schema(
            description = "The reaction type to apply",
            example = "CLAP",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Reaction type is required")
    private ReactionType reactionType;
}
