package com.edutrack.config;

import com.edutrack.entity.User;
import com.edutrack.enums.Role;
import com.edutrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DevAdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail(adminEmail)) {
            System.out.println(
                    "Development admin already exists: " + adminEmail
            );
            return;
        }

        User admin = new User();

        admin.setName("EduTrack Administrator");
        admin.setEmail(adminEmail);
        admin.setPassword(
                passwordEncoder.encode(adminPassword)
        );
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        userRepository.save(admin);

        System.out.println(
                "=============================================="
        );
        System.out.println(
                "EduTrack development ADMIN created"
        );
        System.out.println(
                "Email: " + adminEmail
        );
        System.out.println(
                "=============================================="
        );
    }
}