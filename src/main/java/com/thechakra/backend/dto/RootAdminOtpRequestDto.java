package com.thechakra.backend.dto;

import lombok.Data;

@Data
public class RootAdminOtpRequestDto {
    private String rootSecret;
    private String candidateEmail;
    private String candidateName;
}
