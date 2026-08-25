package com.edutrack.dto;

import java.time.LocalDate;

public class TrainerBatchResponse {

    private Long id;
    private String name;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;

    public TrainerBatchResponse(
            Long id,
            String name,
            String courseName,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.id = id;
        this.name = name;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCourseName() {
        return courseName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
