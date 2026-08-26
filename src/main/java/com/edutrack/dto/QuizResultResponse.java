package com.edutrack.dto;

import java.time.LocalDateTime;

public class QuizResultResponse {

    private Long attemptId;

    private Long quizId;

    private String quizTitle;

    private String traineeName;

    private Integer score;

    private Integer totalQuestions;

    private Double percentage;

    private Integer attemptNumber;

    private LocalDateTime submittedAt;

    public QuizResultResponse(
            Long attemptId,
            Long quizId,
            String quizTitle,
            String traineeName,
            Integer score,
            Integer totalQuestions,
            Double percentage,
            Integer attemptNumber,
            LocalDateTime submittedAt
    ) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.traineeName = traineeName;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.attemptNumber = attemptNumber;
        this.submittedAt = submittedAt;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public String getTraineeName() {
        return traineeName;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public Double getPercentage() {
        return percentage;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}