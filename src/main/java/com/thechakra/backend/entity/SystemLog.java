package com.thechakra.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String actionType; // e.g., "LOGIN", "ADMIN_DELETE_USER", "TEST_COMPLETED"

    @Column(nullable = false)
    private String actionDetails;

    @Column(nullable = true)
    private String performedBy; // Email or UUID of the user who performed the action

    @Column(nullable = true)
    private String targetEntity; // Email or UUID of the affected user/entity
}
