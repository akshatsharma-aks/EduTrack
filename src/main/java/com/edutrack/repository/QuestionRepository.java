package com.edutrack.repository;

import com.edutrack.entity.Question;
import com.edutrack.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    List<Question> findByQuizOrderByIdAsc(
            Quiz quiz
    );
}