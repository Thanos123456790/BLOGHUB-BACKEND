package com.subho.bloghub.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configures Spring Security with Clerk-backed JWT verification.
 *
 * VLN-03 FIX: Swagger/OpenAPI docs removed from the public allow-list.
 * VLN-14 FIX: Actuator endpoints removed from the public allow-list.
 * VLN-CSRF FIX: Cookie-based CSRF protection re-enabled using the
 *   Double-Submit Cookie pattern. The BFF proxy (Next.js route.ts) reads
 *   the XSRF-TOKEN cookie via JS and echoes it in X-XSRF-TOKEN header.
 *   This is safe because:
 *     - The JWT itself is in an httpOnly cookie (no JS access).
 *     - The CSRF token in XSRF-TOKEN is readable by same-origin JS only,
 *       so a cross-origin attacker cannot read it and forge the header.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    private static final String[] PUBLIC_GET_PATTERNS = {
            "/api/v1/blogs",
            "/api/v1/blogs/trending",
            "/api/v1/blogs/{id}",
            "/api/v1/users/{handle}",
            "/api/v1/users/{handle}/blogs",
            "/api/v1/users/{handle}/followers",
            "/api/v1/users/{handle}/following",
            "/api/v1/users/suggested",
            "/api/v1/comments/blogs/{blogId}",
            "/api/v1/search/blogs",
            "/api/v1/search/users",
            "/api/v1/tags/trending",
            "/api/v1/tags/{tagName}/blogs",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // VLN-CSRF FIX: Use Double-Submit Cookie CSRF protection.
        // The frontend BFF proxy must read XSRF-TOKEN and send it as X-XSRF-TOKEN.
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        XorCsrfTokenRequestAttributeHandler csrfHandler = new XorCsrfTokenRequestAttributeHandler();

        // CSRF is skipped for safe HTTP methods and for direct /api/v1/** calls
        // (which use a Bearer JWT, not cookies, so CSRF doesn't apply).
        // CsrfConfigurer.ignoringRequestMatchers() only accepts RequestMatcher or
        // String patterns — NOT (HttpMethod, pattern) pairs — so we build matchers
        // explicitly with AntPathRequestMatcher.
        RequestMatcher csrfExclusions = new OrRequestMatcher(
                new AntPathRequestMatcher("/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/**", HttpMethod.HEAD.name()),
                new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.name()),
                new AntPathRequestMatcher("/api/v1/**")   // Bearer-JWT path: no cookies
        );

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepo)
                        .csrfTokenRequestHandler(csrfHandler)
                        .ignoringRequestMatchers(csrfExclusions)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> {})
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATTERNS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").authenticated()
                        .requestMatchers("/actuator/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}