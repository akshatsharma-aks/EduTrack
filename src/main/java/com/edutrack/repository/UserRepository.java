package com.edutrack.repository;

import com.edutrack.entity.User;
import com.edutrack.enums.Role;
import com.edutrack.enums.TrainerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    long countByRoleAndTrainerStatus(
            Role role,
            TrainerStatus trainerStatus
    );
}