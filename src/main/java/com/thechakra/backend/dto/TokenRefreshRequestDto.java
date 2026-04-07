package com.thechakra.backend.dto;

import lombok.Data;

@Data
public class TokenRefreshRequestDto {
    private String refreshToken;
}
