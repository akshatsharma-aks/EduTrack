package com.edutrack.repository;

import com.edutrack.entity.Batch;
import com.edutrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository
        extends JpaRepository<Batch, Long> {

    long countByEndDateGreaterThanEqual(
            java.time.LocalDate date
    );

    List<Batch> findByTrainer(User trainer);
}