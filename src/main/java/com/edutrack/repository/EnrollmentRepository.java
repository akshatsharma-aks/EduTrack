package com.edutrack.repository;

import com.edutrack.entity.Enrollment;
import com.edutrack.entity.User;
import com.edutrack.entity.Batch;
import com.edutrack.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long> {

    boolean existsByTraineeAndBatch(
            User trainee,
            Batch batch
    );

    Optional<Enrollment> findByTraineeAndBatch(
            User trainee,
            Batch batch
    );

    List<Enrollment> findByTrainee(
            User trainee
    );

    List<Enrollment> findByBatchAndStatus(
            Batch batch,
            EnrollmentStatus status
    );

    List<Enrollment> findByBatch(
            Batch batch
    );
}