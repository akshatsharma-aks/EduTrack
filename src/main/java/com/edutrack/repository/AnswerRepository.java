package com.edutrack.repository;

import com.edutrack.entity.Answer;
import com.edutrack.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository
        extends JpaRepository<Answer, Long> {

    List<Answer> findByAttempt(
            QuizAttempt attempt
    );
}