package com.edutrack.dto;

import com.edutrack.enums.EnrollmentStatus;

import java.time.LocalDate;

public class EnrollmentRequestResponse {

    private Long enrollmentId;
    private Long traineeId;
    private String traineeName;
    private String traineeEmail;
    private Long batchId;
    private String batchName;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;

    public EnrollmentRequestResponse(
            Long enrollmentId,
            Long traineeId,
            String traineeName,
            String traineeEmail,
            Long batchId,
            String batchName,
            String courseName,
            LocalDate startDate,
            LocalDate endDate,
            EnrollmentStatus status
    ) {
        this.enrollmentId = enrollmentId;
        this.traineeId = traineeId;
        this.traineeName = traineeName;
        this.traineeEmail = traineeEmail;
        this.batchId = batchId;
        this.batchName = batchName;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public String getTraineeName() {
        return traineeName;
    }

    public String getTraineeEmail() {
        return traineeEmail;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }
}