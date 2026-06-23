package com.subho.bloghub.server.common;

import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the currently authenticated caller's internal DB {@link UUID} from
 * a raw {@code Authorization: Bearer <token>} header value. Uses Spring's
 * {@link JwtDecoder} (configured against Clerk's JWKS endpoint) for stateless
 * cryptographic verification — no Clerk SDK or Clerk API call required.
 *
 * <p>On the first request from a given Clerk user, {@link ClerkUserProvisioner}
 * transparently creates a {@link Users} row so FK constraints on blog/comment
 * authors are satisfied from day one.
 *
 * <h3>Token format expected</h3>
 * Controllers bind {@code @RequestHeader("Authorization")} including the
 * "Bearer " prefix. This class strips that prefix before decoding.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final JwtDecoder jwtDecoder;
    private final ClerkUserProvisioner provisioner;

    /**
     * Resolves the current user's UUID, throwing a 401 if the token is absent,
     * expired, or cryptographically invalid.
     * Use on write endpoints (create blog, post comment, react, follow, …).
     */
    public UUID requireCurrentUserId(String accessToken) {
        return resolveUser(accessToken, true).getId();
    }

    /**
     * Best-effort resolution — returns {@code null} instead of throwing when no
     * valid token is present. Use on public-read endpoints that personalise their
     * response when a caller IS known (myReaction, bookmarked, isFollowing).
     */
    public UUID resolveCurrentUserIdOrNull(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        try {
            return resolveUser(accessToken, false).getId();
        } catch (Exception e) {
            log.debug("Token present but resolution failed (treated as anonymous): {}", e.getMessage());
            return null;
        }
    }

    // ── private ──────────────────────────────────────────────────────────

    private Users resolveUser(String rawToken, boolean strict) {
        if (rawToken == null || rawToken.isBlank()) {
            if (strict) {
                throw new ApplicationException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                        "Authorization header is required");
            }
            return null;
        }

        String token = stripBearer(rawToken);

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException e) {
            if (strict) {
                log.warn("JWT decode failed: {}", e.getMessage());
                throw new ApplicationException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN",
                        "Access token is invalid or has expired");
            }
            return null;
        }

        // Provision or load the local Users record (idempotent after first request).
        return provisioner.provisionOrGet(jwt);
    }

    private static String stripBearer(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return header != null ? header.trim() : "";
    }
}
