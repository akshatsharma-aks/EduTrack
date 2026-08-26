package com.edutrack.dto;

import java.util.List;

public class QuizAnswerRequest {

    private Long questionId;

    private List<Long> selectedOptionIds;

    public QuizAnswerRequest() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public List<Long> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(
            List<Long> selectedOptionIds
    ) {
        this.selectedOptionIds =
                selectedOptionIds;
    }
}