package com.edutrack.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_option_id", nullable = false)
    private Option selectedOption;


    public Answer() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public QuizAttempt getAttempt() {
        return attempt;
    }


    public void setAttempt(
            QuizAttempt attempt
    ) {
        this.attempt = attempt;
    }


    public Question getQuestion() {
        return question;
    }


    public void setQuestion(
            Question question
    ) {
        this.question = question;
    }


    public Option getSelectedOption() {
        return selectedOption;
    }


    public void setSelectedOption(
            Option selectedOption
    ) {
        this.selectedOption =
                selectedOption;
    }
}