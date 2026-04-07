package com.thechakra.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mentor_tasks")
public class MentorTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID adminId;

    @Column(nullable = false)
    private String adminName;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String resourceUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
