package com.edutrack.repository;

import com.edutrack.entity.Quiz;
import com.edutrack.entity.QuizAttempt;
import com.edutrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository
        extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt>
    findByTraineeOrderBySubmittedAtDesc(
            User trainee
    );

    List<QuizAttempt>
    findByQuizOrderBySubmittedAtDesc(
            Quiz quiz
    );

    long countByQuizAndTrainee(
            Quiz quiz,
            User trainee
    );
}