package com.edutrack.dto;

public class TrainerProgressResponse {

    private Long traineeId;

    private String traineeName;

    private String traineeEmail;

    private Long lectureId;

    private String lectureTitle;

    private Double percentageWatched;

    private Boolean completed;

    private String status;


    public TrainerProgressResponse(
            Long traineeId,
            String traineeName,
            String traineeEmail,
            Long lectureId,
            String lectureTitle,
            Double percentageWatched,
            Boolean completed
    ) {

        this.traineeId =
                traineeId;

        this.traineeName =
                traineeName;

        this.traineeEmail =
                traineeEmail;

        this.lectureId =
                lectureId;

        this.lectureTitle =
                lectureTitle;

        this.percentageWatched =
                percentageWatched;

        this.completed =
                completed;


        if (Boolean.TRUE.equals(completed)) {

            this.status =
                    "Completed";

        } else if (
                percentageWatched != null &&
                        percentageWatched > 0
        ) {

            this.status =
                    "In Progress";

        } else {

            this.status =
                    "Not Started";
        }
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


    public Long getLectureId() {
        return lectureId;
    }


    public String getLectureTitle() {
        return lectureTitle;
    }


    public Double getPercentageWatched() {
        return percentageWatched;
    }


    public Boolean getCompleted() {
        return completed;
    }


    public String getStatus() {
        return status;
    }
}