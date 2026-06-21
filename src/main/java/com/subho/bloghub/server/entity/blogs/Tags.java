package com.subho.bloghub.server.entity.blogs;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tags_name", columnList = "name", unique = true)
})
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;
}
