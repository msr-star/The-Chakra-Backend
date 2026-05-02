package com.thechakra.backend.dto;

import lombok.Data;

@Data
public class GoogleLoginRequestDto {
    private String credential; // The JWT token from Google
    private String adminCode;  // Optional: If they are trying to register as an admin while signing in
}
