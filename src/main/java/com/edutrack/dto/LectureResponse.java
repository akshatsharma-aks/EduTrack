package com.edutrack.dto;

import java.time.LocalDateTime;

public class LectureResponse {

    private Long id;
    private String title;
    private String description;
    private String originalFileName;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private Long batchId;
    private String batchName;

    public LectureResponse(
            Long id,
            String title,
            String description,
            String originalFileName,
            Long fileSize,
            LocalDateTime uploadedAt,
            Long batchId,
            String batchName
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.batchId = batchId;
        this.batchName = batchName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchName() {
        return batchName;
    }
}
