package com.edutrack.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "quiz_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attempt_quiz_trainee",
                        columnNames = {
                                "quiz_id",
                                "trainee_id",
                                "attempt_number"
                        }
                )
        }
)
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private User trainee;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Double percentage;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime submittedAt;

    @OneToMany(
            mappedBy = "attempt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Answer> answers =
            new ArrayList<>();


    @PrePersist
    protected void onCreate() {

        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }


    public QuizAttempt() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Quiz getQuiz() {
        return quiz;
    }


    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }


    public User getTrainee() {
        return trainee;
    }


    public void setTrainee(User trainee) {
        this.trainee = trainee;
    }


    public Integer getAttemptNumber() {
        return attemptNumber;
    }


    public void setAttemptNumber(
            Integer attemptNumber
    ) {
        this.attemptNumber =
                attemptNumber;
    }


    public Integer getScore() {
        return score;
    }


    public void setScore(Integer score) {
        this.score = score;
    }


    public Integer getTotalQuestions() {
        return totalQuestions;
    }


    public void setTotalQuestions(
            Integer totalQuestions
    ) {
        this.totalQuestions =
                totalQuestions;
    }


    public Double getPercentage() {
        return percentage;
    }


    public void setPercentage(
            Double percentage
    ) {
        this.percentage =
                percentage;
    }


    public LocalDateTime getStartedAt() {
        return startedAt;
    }


    public void setStartedAt(
            LocalDateTime startedAt
    ) {
        this.startedAt =
                startedAt;
    }


    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }


    public void setSubmittedAt(
            LocalDateTime submittedAt
    ) {
        this.submittedAt =
                submittedAt;
    }


    public List<Answer> getAnswers() {
        return answers;
    }


    public void setAnswers(
            List<Answer> answers
    ) {
        this.answers = answers;
    }


    public void addAnswer(
            Answer answer
    ) {

        answers.add(answer);

        answer.setAttempt(this);
    }
}