package com.thechakra.backend.config;

import com.thechakra.backend.entity.Role;
import com.thechakra.backend.entity.User;
import com.thechakra.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@thechakra.com")) {
            User admin = User.builder()
                    .name("Supreme Architect")
                    .email("admin@thechakra.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("student@thechakra.com")) {
            User student = User.builder()
                    .name("Test Subject Alpha")
                    .email("student@thechakra.com")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .build();
            userRepository.save(student);
        }
    }
}
