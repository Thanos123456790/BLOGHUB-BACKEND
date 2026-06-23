package com.subho.bloghub.server.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VLN-05 FIX: In-memory per-user rate limiter using Bucket4j.
 *
 * Two buckets per user:
 *  - write bucket: caps mutating requests (POST/PUT/PATCH/DELETE) per minute
 *  - upload bucket: caps asset uploads per hour
 *
 * The bucket map is kept in a ConcurrentHashMap. For a multi-node deployment,
 * replace with Bucket4j's JCache/Redis integration; the rest of the logic stays
 * identical. For a single-node deployment this is production-ready.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> WRITE_METHODS =
            Set.of("POST", "PUT", "PATCH", "DELETE");

    private static final String UPLOAD_PATH = "/api/v1/assets/upload";

    private final RateLimitProperties props;
    private final JwtDecoder jwtDecoder;

    // bucketKey -> Bucket
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        String method = request.getMethod();
        if (!WRITE_METHODS.contains(method)) {
            chain.doFilter(request, response);
            return;
        }

        String userId = resolveUserId(request);
        if (userId == null) {
            // Unauthenticated — Spring Security will reject it anyway; skip rate limit.
            chain.doFilter(request, response);
            return;
        }

        boolean isUpload = request.getRequestURI().startsWith(UPLOAD_PATH);
        String bucketKey = isUpload ? "upload:" + userId : "write:" + userId;

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> isUpload
                ? buildUploadBucket()
                : buildWriteBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for user {} on {} {}", userId, method, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"RATE_LIMIT_EXCEEDED\"," +
                    "\"message\":\"Too many requests. Please slow down and try again shortly.\"}");
        }
    }

    private Bucket buildWriteBucket() {
        Bandwidth limit = Bandwidth.classic(
                props.getWritePerUserPerMinute(),
                Refill.greedy(props.getWritePerUserPerMinute(), Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket buildUploadBucket() {
        Bandwidth limit = Bandwidth.classic(
                props.getAssetUploadPerUserPerHour(),
                Refill.greedy(props.getAssetUploadPerUserPerHour(), Duration.ofHours(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /** Extracts the Clerk user ID from the Authorization header without throwing. */
    private String resolveUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        try {
            Jwt jwt = jwtDecoder.decode(header.substring(7).trim());
            return jwt.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}
