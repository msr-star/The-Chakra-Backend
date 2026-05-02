package com.thechakra.backend.service;

import com.thechakra.backend.dto.*;
import com.thechakra.backend.entity.RefreshToken;
import com.thechakra.backend.entity.Role;
import com.thechakra.backend.entity.User;
import com.thechakra.backend.entity.VerificationToken;
import com.thechakra.backend.repository.UserRepository;
import com.thechakra.backend.repository.VerificationTokenRepository;
import com.thechakra.backend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final VerificationTokenRepository verificationTokenRepository;
        private final EmailService emailService;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtils jwtUtils;
        private final AuthenticationManager authenticationManager;
        private final UserMapper userMapper;
        private final RefreshTokenService refreshTokenService;

        private String generateOtp() {
                return String.format("%06d", new Random().nextInt(999999));
        }

        @Transactional
        public void requestAdminAccess(String name, String email) {
                emailService.sendAdminApprovalRequest(email, name);
        }

        @Transactional
        public void generateAdminOtp(RootAdminOtpRequestDto request) {
                if (!"CHAKRA_ADMIN_777".equals(request.getRootSecret())) {
                        throw new IllegalArgumentException("Invalid Root Secret");
                }

                verificationTokenRepository.deleteByEmailAndTokenType(request.getCandidateEmail(),
                                VerificationToken.TokenType.ADMIN_REGISTRATION);

                String otp = generateOtp();
                VerificationToken token = VerificationToken.builder()
                                .email(request.getCandidateEmail())
                                .token(otp)
                                .tokenType(VerificationToken.TokenType.ADMIN_REGISTRATION)
                                .expiryDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(24))
                                .build();
                verificationTokenRepository.save(token);

                emailService.sendEmail(request.getCandidateEmail(), "Your Admin Access Code",
                                "The Root Authority has approved your request. Your Admin Access Code is: " + otp);
        }

        @Transactional
        public AuthResponseDto register(RegisterRequestDto request) {
                boolean isAdminRegistration = request.getAdminCode() != null
                                && !request.getAdminCode().trim().isEmpty();

                if (isAdminRegistration) {
                        Optional<VerificationToken> otpOpt = verificationTokenRepository
                                        .findByEmailAndTokenType(request.getEmail(),
                                                        VerificationToken.TokenType.ADMIN_REGISTRATION);

                        if (otpOpt.isPresent()) {
                                VerificationToken token = otpOpt.get();
                                if (!token.getToken().equals(request.getAdminCode())) {
                                        throw new IllegalArgumentException("Invalid Admin Code");
                                }
                                if (token.getExpiryDate().isBefore(LocalDateTime.now(ZoneId.of("UTC")))) {
                                        throw new IllegalArgumentException("Expired Admin Code");
                                }
                                verificationTokenRepository.delete(token);
                        } else {
                                throw new IllegalArgumentException("Invalid Admin Code generated");
                        }
                }

                Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
                User user;

                if (existingUserOpt.isPresent()) {
                        user = existingUserOpt.get();

                        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty() &&
                                        !request.getPhoneNumber().equals(user.getPhoneNumber()) &&
                                        userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                                throw new IllegalArgumentException("Phone number already in use by another account");
                        }

                        if (isAdminRegistration) {
                                user.setRole(Role.ADMIN);
                                user.setName(request.getName());
                                user.setPassword(passwordEncoder.encode(request.getPassword()));

                                String phoneToSave = (request.getPhoneNumber() != null
                                                && request.getPhoneNumber().trim().isEmpty())
                                                                ? null
                                                                : request.getPhoneNumber();
                                user.setPhoneNumber(phoneToSave);
                                userRepository.save(user);
                        } else {
                                throw new IllegalArgumentException("Email already in use");
                        }
                } else {
                        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()
                                        && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                                throw new IllegalArgumentException("Phone number already in use");
                        }

                        String phoneToSave = (request.getPhoneNumber() != null
                                        && request.getPhoneNumber().trim().isEmpty())
                                                        ? null
                                                        : request.getPhoneNumber();

                        user = User.builder()
                                        .name(request.getName())
                                        .email(request.getEmail())
                                        .phoneNumber(phoneToSave)
                                        .password(passwordEncoder.encode(request.getPassword()))
                                        .role(isAdminRegistration ? Role.ADMIN : Role.STUDENT)
                                        .build();

                        userRepository.save(user);
                }

                String jwtToken = jwtUtils.generateToken(user.getEmail());
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

                return AuthResponseDto.builder()
                                .token(jwtToken)
                                .refreshToken(refreshToken.getToken())
                                .user(userMapper.toDto(user))
                                .build();
        }

        @Transactional
        public AuthResponseDto login(LoginRequestDto request) {
                User user = userRepository.findByEmailOrPhoneNumber(request.getIdentifier(), request.getIdentifier())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid email/phone or password"));

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword()));

                if (user.getRole() == Role.ADMIN) {
                        verificationTokenRepository.deleteByEmailAndTokenType(user.getEmail(),
                                        VerificationToken.TokenType.ADMIN_LOGIN);
                        String otp = generateOtp();
                        VerificationToken token = VerificationToken.builder()
                                        .email(user.getEmail())
                                        .token(otp)
                                        .tokenType(VerificationToken.TokenType.ADMIN_LOGIN)
                                        .expiryDate(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(30))
                                        .build();
                        verificationTokenRepository.save(token);

                        emailService.sendEmail(user.getEmail(), "Admin Login Verification",
                                        "Your OTP for login is: " + otp);

                        return AuthResponseDto.builder()
                                        .message("OTP_SENT")
                                        .user(userMapper.toDto(user))
                                        .build();
                }

                String jwtToken = jwtUtils.generateToken(user.getEmail());
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

                return AuthResponseDto.builder()
                                .token(jwtToken)
                                .refreshToken(refreshToken.getToken())
                                .user(userMapper.toDto(user))
                                .build();
        }

        @Transactional
        public AuthResponseDto verifyAdminLogin(VerifyOtpRequestDto request) {
                // Resolve the identifier (email OR phone number) to the actual user,
                // because the OTP was stored under the user's real email address.
                User user = userRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                String actualEmail = user.getEmail(); // always the stored email

                Optional<VerificationToken> dbTokenOpt = verificationTokenRepository
                                .findByEmailAndTokenType(actualEmail, VerificationToken.TokenType.ADMIN_LOGIN);

                if (dbTokenOpt.isPresent()) {
                        VerificationToken token = dbTokenOpt.get();
                        log.debug("[DEBUG] Comparing Input: {} with DB Token: {} for Type: {}", request.getOtp(),
                                        token.getToken(), VerificationToken.TokenType.ADMIN_LOGIN);

                        if (!token.getToken().equals(request.getOtp())) {
                                throw new IllegalArgumentException("Invalid OTP");
                        }

                        if (token.getExpiryDate().isBefore(LocalDateTime.now(ZoneId.of("UTC")))) {
                                throw new IllegalArgumentException("OTP expired");
                        }

                        verificationTokenRepository.delete(token);
                } else {
                        log.debug("[DEBUG] No DB Token found for email: {} and Type: {}", actualEmail,
                                        VerificationToken.TokenType.ADMIN_LOGIN);
                        throw new IllegalArgumentException("Invalid OTP");
                }

                String jwtToken = jwtUtils.generateToken(user.getEmail());
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

                return AuthResponseDto.builder()
                                .token(jwtToken)
                                .refreshToken(refreshToken.getToken())
                                .user(userMapper.toDto(user))
                                .build();
        }

        @Transactional
        public void forgotPassword(ForgotPasswordRequestDto request) {
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                verificationTokenRepository.deleteByEmailAndTokenType(user.getEmail(),
                                VerificationToken.TokenType.FORGOT_PASSWORD);

                String otp = generateOtp();
                VerificationToken token = VerificationToken.builder()
                                .email(user.getEmail())
                                .token(otp)
                                .tokenType(VerificationToken.TokenType.FORGOT_PASSWORD)
                                .expiryDate(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(30))
                                .build();
                verificationTokenRepository.save(token);

                emailService.sendEmail(user.getEmail(), "Password Reset", "Your password reset code is: " + otp);
        }

        @Transactional
        public void resetPassword(ResetPasswordRequestDto request) {
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Optional<VerificationToken> dbTokenOpt = verificationTokenRepository
                                .findByEmailAndTokenType(request.getEmail(),
                                                VerificationToken.TokenType.FORGOT_PASSWORD);

                if (dbTokenOpt.isPresent()) {
                        VerificationToken token = dbTokenOpt.get();
                        log.debug("[DEBUG] Comparing Input: {} with DB Token: {} for Type: {}", request.getOtp(),
                                        token.getToken(), VerificationToken.TokenType.FORGOT_PASSWORD);

                        if (!token.getToken().equals(request.getOtp())) {
                                throw new IllegalArgumentException("Invalid OTP");
                        }

                        if (token.getExpiryDate().isBefore(LocalDateTime.now(ZoneId.of("UTC")))) {
                                throw new IllegalArgumentException("OTP expired");
                        }

                        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                        userRepository.save(user);
                        verificationTokenRepository.delete(token);
                } else {
                        log.debug("[DEBUG] No DB Token found for Input: {} and Type: {}", request.getOtp(),
                                        VerificationToken.TokenType.FORGOT_PASSWORD);
                        throw new IllegalArgumentException("Invalid OTP");
                }
        }

        @Transactional
        public TokenRefreshResponseDto refreshToken(TokenRefreshRequestDto request) {
                String requestRefreshToken = request.getRefreshToken();
                return refreshTokenService.findByToken(requestRefreshToken)
                                .map(refreshTokenService::verifyExpiration)
                                .map(RefreshToken::getUser)
                                .map(user -> new TokenRefreshResponseDto(jwtUtils.generateToken(user.getEmail()),
                                                requestRefreshToken))
                                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
        }
}
