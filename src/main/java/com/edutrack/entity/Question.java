package com.edutrack.entity;

import com.edutrack.enums.QuestionType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Option> options =
            new ArrayList<>();


    public Question() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getQuestionText() {
        return questionText;
    }


    public void setQuestionText(
            String questionText
    ) {
        this.questionText =
                questionText;
    }


    public QuestionType getType() {
        return type;
    }


    public void setType(
            QuestionType type
    ) {
        this.type = type;
    }


    public Quiz getQuiz() {
        return quiz;
    }


    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }


    public List<Option> getOptions() {
        return options;
    }


    public void setOptions(
            List<Option> options
    ) {
        this.options = options;
    }


    public void addOption(
            Option option
    ) {

        options.add(option);

        option.setQuestion(this);
    }


    public void removeOption(
            Option option
    ) {

        options.remove(option);

        option.setQuestion(null);
    }
}