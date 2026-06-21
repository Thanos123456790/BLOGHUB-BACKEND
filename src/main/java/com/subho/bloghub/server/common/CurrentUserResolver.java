package com.subho.bloghub.server.common;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Single shared placeholder for resolving the caller's identity from an
 * access token. Authentication/authorization is explicitly out of scope
 * right now — every module that needs "who is the current user" (to
 * persist an author/actor, check ownership, or compute personalized fields
 * like {@code isFollowing}/{@code myReaction}/{@code bookmarked}) calls
 * through here, so the day real auth is implemented there is exactly one
 * place to change.
 *
 * Endpoints that require a caller identity to do their job (creating a
 * blog, reacting, commenting, etc.) will throw via {@link #requireCurrentUserId}
 * until this is wired up. Endpoints where identity is optional (most public
 * GETs) should treat a missing/unresolvable token as "anonymous" rather
 * than failing — see callers for the exact pattern.
 */
@Component
public class CurrentUserResolver {

    /**
     * Resolves the current user id, throwing if it cannot be resolved.
     * Use on endpoints where identity is mandatory for the operation to
     * make sense (create blog, post comment, react, follow, bookmark...).
     */
    public UUID requireCurrentUserId(String accessToken) {
        throw new UnsupportedOperationException(
                "Access token resolution is not implemented yet. accessToken is currently " +
                        "threaded through the API/controller layer only, per project scope.");
    }

    /**
     * Best-effort resolution for endpoints where identity is optional (e.g.
     * public blog/comment reads that personalize myReaction/bookmarked when
     * a caller IS known). Returns null instead of throwing — callers must
     * treat null as "anonymous viewer".
     */
    public UUID resolveCurrentUserIdOrNull(String accessToken) {
        return null;
    }
}
