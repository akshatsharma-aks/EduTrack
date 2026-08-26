package com.edutrack.dto;

import java.time.LocalDateTime;

public class VideoProgressResponse {

    private Long lectureId;

    private Double currentPosition;

    private Double percentageWatched;

    private Boolean completed;

    private LocalDateTime lastWatchedAt;


    public VideoProgressResponse(
            Long lectureId,
            Double currentPosition,
            Double percentageWatched,
            Boolean completed,
            LocalDateTime lastWatchedAt
    ) {

        this.lectureId =
                lectureId;

        this.currentPosition =
                currentPosition;

        this.percentageWatched =
                percentageWatched;

        this.completed =
                completed;

        this.lastWatchedAt =
                lastWatchedAt;
    }


    public Long getLectureId() {
        return lectureId;
    }


    public Double getCurrentPosition() {
        return currentPosition;
    }


    public Double getPercentageWatched() {
        return percentageWatched;
    }


    public Boolean getCompleted() {
        return completed;
    }


    public LocalDateTime getLastWatchedAt() {
        return lastWatchedAt;
    }
}