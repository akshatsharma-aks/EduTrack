package com.edutrack.service;

import com.edutrack.dto.LoginRequest;
import com.edutrack.dto.LoginResponse;
import com.edutrack.entity.User;
import com.edutrack.enums.Role;
import com.edutrack.enums.TrainerStatus;
import com.edutrack.repository.UserRepository;
import com.edutrack.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "This account is disabled"
            );
        }

        if (user.getRole() == Role.TRAINER
                && user.getTrainerStatus()
                != TrainerStatus.APPROVED) {

            if (user.getTrainerStatus()
                    == TrainerStatus.PENDING) {

                throw new IllegalArgumentException(
                        "Your trainer registration is pending admin approval"
                );
            }

            throw new IllegalArgumentException(
                    "Your trainer registration was declined"
            );
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        String token =
                jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}