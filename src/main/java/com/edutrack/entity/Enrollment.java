package com.edutrack.entity;

import com.edutrack.enums.EnrollmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_enrollment_trainee_batch",
                        columnNames = {
                                "trainee_id",
                                "batch_id"
                        }
                )
        }
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trainee_id",
            nullable = false
    )
    private User trainee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "batch_id",
            nullable = false
    )
    private Batch batch;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private EnrollmentStatus status;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {

        requestedAt = LocalDateTime.now();

        if (status == null) {
            status = EnrollmentStatus.PENDING;
        }
    }

    public Enrollment() {
    }

    public Long getId() {
        return id;
    }

    public User getTrainee() {
        return trainee;
    }

    public void setTrainee(User trainee) {
        this.trainee = trainee;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}