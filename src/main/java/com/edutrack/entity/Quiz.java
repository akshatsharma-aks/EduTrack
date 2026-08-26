package com.edutrack.entity;

import com.edutrack.enums.QuizStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private Integer timeLimitMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuizStatus status = QuizStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "quiz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Question> questions =
            new ArrayList<>();


    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = QuizStatus.DRAFT;
        }
    }


    public Quiz() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }


    public void setTimeLimitMinutes(
            Integer timeLimitMinutes
    ) {
        this.timeLimitMinutes =
                timeLimitMinutes;
    }


    public QuizStatus getStatus() {
        return status;
    }


    public void setStatus(QuizStatus status) {
        this.status = status;
    }


    public Batch getBatch() {
        return batch;
    }


    public void setBatch(Batch batch) {
        this.batch = batch;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }


    public List<Question> getQuestions() {
        return questions;
    }


    public void setQuestions(
            List<Question> questions
    ) {
        this.questions = questions;
    }


    public void addQuestion(
            Question question
    ) {

        questions.add(question);

        question.setQuiz(this);
    }


    public void removeQuestion(
            Question question
    ) {

        questions.remove(question);

        question.setQuiz(null);
    }
}