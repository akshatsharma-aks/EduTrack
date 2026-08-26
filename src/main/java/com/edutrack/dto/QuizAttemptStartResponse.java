package com.edutrack.dto;

public class QuizAttemptStartResponse {

    private Long attemptId;

    private Long quizId;

    private String quizTitle;

    private Integer timeLimitMinutes;

    private Long startedAt;

    private QuizResponse quiz;

    public QuizAttemptStartResponse(
            Long attemptId,
            Long quizId,
            String quizTitle,
            Integer timeLimitMinutes,
            Long startedAt,
            QuizResponse quiz
    ) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.timeLimitMinutes = timeLimitMinutes;
        this.startedAt = startedAt;
        this.quiz = quiz;
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

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public QuizResponse getQuiz() {
        return quiz;
    }
}