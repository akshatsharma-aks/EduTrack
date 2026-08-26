package com.edutrack.dto;

import java.util.List;

public class QuizCreateRequest {

    private String title;

    private Integer timeLimitMinutes;

    private Long batchId;

    private List<QuestionRequest> questions;


    public QuizCreateRequest() {
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(
            Integer timeLimitMinutes
    ) {
        this.timeLimitMinutes =
                timeLimitMinutes;
    }


    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }


    public List<QuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(
            List<QuestionRequest> questions
    ) {
        this.questions = questions;
    }
}