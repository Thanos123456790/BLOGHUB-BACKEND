package com.subho.bloghub.client.dtos.comments;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to post a comment or reply")
public class CreateCommentRequestDTO {

    @Schema(
            description = "Comment text content. Use @handle to mention users.",
            example = "This matches my experience. @arjunwrites did your team set up a style guide?",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String content;

    @Schema(
            description = "UUIDs of @mentioned users in this comment (must match @handles present in content)",
            example = "[\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"]"
    )
    @Size(max = 10, message = "Cannot mention more than 10 users in a single comment")
    private List<UUID> taggedUserIds;
}
