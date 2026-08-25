package com.edutrack.dto;

import java.time.LocalDate;

public class BatchResponse {

    private Long id;
    private String name;
    private Long courseId;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long trainerId;
    private String trainerName;

    public BatchResponse(
            Long id,
            String name,
            Long courseId,
            String courseName,
            LocalDate startDate,
            LocalDate endDate,
            Long trainerId,
            String trainerName
    ) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.trainerId = trainerId;
        this.trainerName = trainerName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCourseId() {
        return courseId;
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

    public Long getTrainerId() {
        return trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }
}