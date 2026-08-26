package com.edutrack.dto;

import com.edutrack.enums.QuizStatus;

public class QuizListResponse {

    private Long id;
    private String title;
    private Integer timeLimitMinutes;
    private QuizStatus status;
    private Long batchId;
    private String batchName;
    private String courseName;
    private Integer questionCount;

    public QuizListResponse(
            Long id,
            String title,
            Integer timeLimitMinutes,
            QuizStatus status,
            Long batchId,
            String batchName,
            String courseName,
            Integer questionCount
    ) {
        this.id = id;
        this.title = title;
        this.timeLimitMinutes = timeLimitMinutes;
        this.status = status;
        this.batchId = batchId;
        this.batchName = batchName;
        this.courseName = courseName;
        this.questionCount = questionCount;
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

    public String getCourseName() {
        return courseName;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }
}