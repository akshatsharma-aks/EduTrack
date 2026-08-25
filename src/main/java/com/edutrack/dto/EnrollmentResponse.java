package com.edutrack.dto;

import com.edutrack.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EnrollmentResponse {

    private Long enrollmentId;
    private Long batchId;
    private String batchName;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;
    private LocalDateTime requestedAt;

    public EnrollmentResponse(
            Long enrollmentId,
            Long batchId,
            String batchName,
            String courseName,
            LocalDate startDate,
            LocalDate endDate,
            EnrollmentStatus status,
            LocalDateTime requestedAt
    ) {
        this.enrollmentId = enrollmentId;
        this.batchId = batchId;
        this.batchName = batchName;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
