package com.edutrack.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "video_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_video_progress_trainee_lecture",
                        columnNames = {
                                "trainee_id",
                                "lecture_id"
                        }
                )
        }
)
public class VideoProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trainee_id",
            nullable = false
    )
    private User trainee;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lecture_id",
            nullable = false
    )
    private Lecture lecture;


    @Column(
            nullable = false
    )
    private Double currentPosition = 0.0;


    @Column(
            nullable = false
    )
    private Double percentageWatched = 0.0;


    @Column(
            nullable = false
    )
    private Boolean completed = false;


    @Column(
            nullable = false
    )
    private LocalDateTime lastWatchedAt;


    public VideoProgress() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public User getTrainee() {
        return trainee;
    }


    public void setTrainee(User trainee) {
        this.trainee = trainee;
    }


    public Lecture getLecture() {
        return lecture;
    }


    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
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


    public Double getPercentageWatched() {
        return percentageWatched;
    }


    public void setPercentageWatched(
            Double percentageWatched
    ) {
        this.percentageWatched =
                percentageWatched;
    }


    public Boolean getCompleted() {
        return completed;
    }


    public void setCompleted(
            Boolean completed
    ) {
        this.completed =
                completed;
    }


    public LocalDateTime getLastWatchedAt() {
        return lastWatchedAt;
    }


    public void setLastWatchedAt(
            LocalDateTime lastWatchedAt
    ) {
        this.lastWatchedAt =
                lastWatchedAt;
    }
}