package com.edutrack.repository;

import com.edutrack.entity.Option;
import com.edutrack.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository
        extends JpaRepository<Option, Long> {

    List<Option> findByQuestionOrderByIdAsc(
            Question question
    );
}