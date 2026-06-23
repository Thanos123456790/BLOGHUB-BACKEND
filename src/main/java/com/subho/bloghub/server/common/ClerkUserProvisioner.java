package com.subho.bloghub.server.common;

import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Auto-provisions a {@link Users} row the first time a Clerk-authenticated user
 * hits any protected endpoint.
 *
 * VLN-17 FIX: Handle generation now checks for uniqueness in the DB and retries
 * with an incrementing numeric suffix until it finds a free handle.
 * VLN-09b FIX: passwordHash is no longer set on new rows.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClerkUserProvisioner {

    private static final int MAX_HANDLE_RETRIES = 10;

    private final UserRepository userRepository;

    @Transactional
    public Users provisionOrGet(Jwt jwt) {
        String clerkUserId = jwt.getSubject();

        Optional<Users> existing = userRepository.findByClerkUserId(clerkUserId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = clerkUserId + "@clerk.placeholder";
        }

        String name      = buildName(jwt);
        String handle    = generateUniqueHandle(name, clerkUserId);
        String avatarUrl = jwt.getClaimAsString("image_url");

        Users user = Users.builder()
                .clerkUserId(clerkUserId)
                .email(email)
                .name(name)
                .handle(handle)
                // VLN-09b FIX: passwordHash intentionally not set — Clerk handles auth
                .avatarUrl(avatarUrl)
                .isVerified(false)
                .build();

        Users saved = userRepository.save(user);
        log.info("AUDIT: Provisioned new Users row {} for Clerk user {}", saved.getId(), clerkUserId);
        return saved;
    }

    private String buildName(Jwt jwt) {
        String given  = jwt.getClaimAsString("given_name");
        String family = jwt.getClaimAsString("family_name");
        if (given != null && family != null) {
            return (given + " " + family).trim();
        }
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        String email = jwt.getClaimAsString("email");
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return "Blogger " + jwt.getSubject().substring(jwt.getSubject().length() - 6);
    }

    private String generateUniqueHandle(String name, String clerkUserId) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .replaceAll("\\s+", "");
        if (base.isBlank()) {
            base = "user";
        }
        String suffix = clerkUserId.length() >= 6
                ? clerkUserId.substring(clerkUserId.length() - 6).replaceAll("[^a-z0-9]", "")
                : UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        String candidate = truncate(base, 44) + suffix;
        if (!userRepository.existsByHandle(candidate)) {
            return candidate;
        }

        for (int i = 2; i <= MAX_HANDLE_RETRIES; i++) {
            String withCounter = truncate(base, 44) + suffix + i;
            if (!userRepository.existsByHandle(withCounter)) {
                return withCounter;
            }
        }

        return truncate(base, 42) + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
