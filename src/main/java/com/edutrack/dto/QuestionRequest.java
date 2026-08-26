package com.edutrack.dto;

import com.edutrack.enums.QuestionType;

import java.util.List;

public class QuestionRequest {

    private String questionText;

    private QuestionType type;

    private List<OptionRequest> options;


    public QuestionRequest() {
    }


    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(
            String questionText
    ) {
        this.questionText =
                questionText;
    }


    public QuestionType getType() {
        return type;
    }

    public void setType(
            QuestionType type
    ) {
        this.type = type;
    }


    public List<OptionRequest> getOptions() {
        return options;
    }

    public void setOptions(
            List<OptionRequest> options
    ) {
        this.options = options;
    }
}