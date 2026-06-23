package com.subho.bloghub.server.entity.users;

import com.subho.bloghub.server.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_users_handle", columnList = "handle", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_clerk_id", columnList = "clerkUserId", unique = true)
})
public class Users extends BaseEntity {

    /**
     * The Clerk User ID (e.g. "user_2abc...") from the JWT's {@code sub} claim.
     */
    @Column(unique = true, length = 255)
    private String clerkUserId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String handle;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * VLN-09b FIX: passwordHash is no longer used — authentication is handled
     * entirely by Clerk. The column is kept nullable for backwards compatibility
     * with existing rows. New rows never write to it.
     *
     * TODO: Remove this column via a DB migration once old rows are cleaned up.
     */
    @Column(columnDefinition = "TEXT", nullable = true)
    private String passwordHash;

    @Column(columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bannerUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isVerified;
}
