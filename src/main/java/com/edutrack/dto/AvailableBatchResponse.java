package com.edutrack.dto;

import java.time.LocalDate;

public class AvailableBatchResponse {

    private Long id;
    private String name;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String trainerName;

    public AvailableBatchResponse(
            Long id,
            String name,
            String courseName,
            LocalDate startDate,
            LocalDate endDate,
            String trainerName
    ) {
        this.id = id;
        this.name = name;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.trainerName = trainerName;
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

    public String getTrainerName() {
        return trainerName;
    }
}