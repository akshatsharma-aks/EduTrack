package com.edutrack.controller;

import com.edutrack.entity.User;
import com.edutrack.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return ResponseEntity.ok(
                new CurrentUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name()
                )
        );
    }

    public record CurrentUserResponse(
            Long id,
            String name,
            String email,
            String role
    ) {
    }
}