package com.edutrack.dto;

import com.edutrack.enums.QuizStatus;

import java.time.LocalDateTime;
import java.util.List;

public class QuizResponse {

    private Long id;

    private String title;

    private Integer timeLimitMinutes;

    private QuizStatus status;

    private Long batchId;

    private String batchName;

    private LocalDateTime createdAt;

    private List<QuestionResponse> questions;


    public QuizResponse(
            Long id,
            String title,
            Integer timeLimitMinutes,
            QuizStatus status,
            Long batchId,
            String batchName,
            LocalDateTime createdAt,
            List<QuestionResponse> questions
    ) {

        this.id = id;
        this.title = title;
        this.timeLimitMinutes =
                timeLimitMinutes;
        this.status = status;
        this.batchId = batchId;
        this.batchName = batchName;
        this.createdAt = createdAt;
        this.questions = questions;
    }


    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchName() {
        return batchName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<QuestionResponse> getQuestions() {
        return questions;
    }
}