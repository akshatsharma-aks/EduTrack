package com.edutrack.dto;

import java.util.List;

public class QuizSubmitRequest {

    private Long attemptId;

    private List<QuizAnswerRequest> answers;

    public QuizSubmitRequest() {
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public List<QuizAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(
            List<QuizAnswerRequest> answers
    ) {
        this.answers = answers;
    }
}