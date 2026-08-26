package com.edutrack.dto;

import com.edutrack.enums.QuestionType;

import java.util.List;

public class QuestionResponse {

    private Long id;

    private String questionText;

    private QuestionType type;

    private List<OptionResponse> options;


    public QuestionResponse(
            Long id,
            String questionText,
            QuestionType type,
            List<OptionResponse> options
    ) {

        this.id = id;
        this.questionText =
                questionText;
        this.type = type;
        this.options = options;
    }


    public Long getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public QuestionType getType() {
        return type;
    }

    public List<OptionResponse> getOptions() {
        return options;
    }
}