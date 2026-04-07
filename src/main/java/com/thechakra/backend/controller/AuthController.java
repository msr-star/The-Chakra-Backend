package com.thechakra.backend.controller;

import com.thechakra.backend.dto.*;
import com.thechakra.backend.service.AuthService;
import com.thechakra.backend.service.SystemAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SystemAuditService systemAuditService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        String roleStr = (request.getAdminCode() != null && !request.getAdminCode().isEmpty()) ? "ADMIN" : "STUDENT";
        systemAuditService.logAction("REGISTER_SUCCESS", "New user registered with role " + roleStr,
                request.getEmail(), request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        systemAuditService.logAction("LOGIN_SUCCESS", "User authenticated", request.getIdentifier(),
                request.getIdentifier());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-admin-login")
    public ResponseEntity<AuthResponseDto> verifyAdminLogin(@RequestBody VerifyOtpRequestDto request) {
        AuthResponseDto response = authService.verifyAdminLogin(request);
        systemAuditService.logAction("ADMIN_LOGIN_VERIFIED", "Admin OTP Verified", request.getEmail(),
                request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-admin-access")
    public ResponseEntity<?> requestAdminAccess(@RequestBody Map<String, String> request) {
        authService.requestAdminAccess(request.get("name"), request.get("email"));
        systemAuditService.logAction("ADMIN_ACCESS_REQUESTED", "Candidate requested root authority approval",
                request.get("email"), "ROOT_AUTHORITY");
        return ResponseEntity.ok(Map.of("message", "Request sent to Root Authority"));
    }

    @PostMapping("/root/generate-admin-otp")
    public ResponseEntity<?> generateAdminOtp(@RequestBody RootAdminOtpRequestDto request) {
        authService.generateAdminOtp(request);
        return ResponseEntity.ok(Map.of("message", "OTP generated and sent to candidate"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<TokenRefreshResponseDto> refreshtoken(@RequestBody TokenRefreshRequestDto request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
