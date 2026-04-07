package com.thechakra.backend.dto;

import com.thechakra.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
    private String chakraAlignment;
    private UUID assignedAdminId;
    private String assignedAdminName;
}
