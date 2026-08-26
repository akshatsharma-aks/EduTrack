package com.edutrack.repository;

import com.edutrack.entity.Batch;
import com.edutrack.entity.Quiz;
import com.edutrack.enums.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository
        extends JpaRepository<Quiz, Long> {

    List<Quiz> findByBatchOrderByCreatedAtDesc(
            Batch batch
    );

    List<Quiz> findByBatchAndStatusOrderByCreatedAtDesc(
            Batch batch,
            QuizStatus status
    );
}