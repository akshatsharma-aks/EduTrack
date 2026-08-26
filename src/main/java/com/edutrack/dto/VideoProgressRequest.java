package com.edutrack.dto;

public class VideoProgressRequest {

    private Double currentPosition;

    private Double duration;


    public VideoProgressRequest() {
    }


    public Double getCurrentPosition() {
        return currentPosition;
    }


    public void setCurrentPosition(
            Double currentPosition
    ) {
        this.currentPosition =
                currentPosition;
    }


    public Double getDuration() {
        return duration;
    }


    public void setDuration(
            Double duration
    ) {
        this.duration =
                duration;
    }
}