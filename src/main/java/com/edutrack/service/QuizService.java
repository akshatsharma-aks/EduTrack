package com.edutrack.service;

import com.edutrack.dto.*;
import com.edutrack.entity.*;
import com.edutrack.enums.QuestionType;
import com.edutrack.enums.QuizStatus;
import com.edutrack.enums.Role;
import com.edutrack.repository.BatchRepository;
import com.edutrack.repository.QuizRepository;
import com.edutrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;


    public QuizService(
            QuizRepository quizRepository,
            BatchRepository batchRepository,
            UserRepository userRepository
    ) {

        this.quizRepository =
                quizRepository;

        this.batchRepository =
                batchRepository;

        this.userRepository =
                userRepository;
    }


    /* =========================
       CREATE QUIZ
    ========================= */

    @Transactional
    public QuizResponse createQuiz(
            String trainerEmail,
            QuizCreateRequest request
    ) {

        User trainer =
                getTrainer(trainerEmail);


        validateQuizRequest(request);


        Batch batch =
                batchRepository
                        .findById(
                                request.getBatchId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found"
                                )
                        );


        verifyTrainerOwnsBatch(
                trainer,
                batch
        );


        Quiz quiz =
                new Quiz();


        quiz.setTitle(
                request.getTitle().trim()
        );


        quiz.setTimeLimitMinutes(
                request.getTimeLimitMinutes()
        );


        quiz.setBatch(batch);


        quiz.setStatus(
                QuizStatus.DRAFT
        );


        /*
         * Questions
         */

        for (
                QuestionRequest questionRequest
                : request.getQuestions()
        ) {

            validateQuestion(
                    questionRequest
            );


            Question question =
                    new Question();


            question.setQuestionText(
                    questionRequest
                            .getQuestionText()
                            .trim()
            );


            question.setType(
                    questionRequest.getType()
            );


            quiz.addQuestion(
                    question
            );


            /*
             * Options
             */

            for (
                    OptionRequest optionRequest
                    : questionRequest.getOptions()
            ) {

                Option option =
                        new Option();


                option.setOptionText(
                        optionRequest
                                .getOptionText()
                                .trim()
                );


                option.setCorrect(
                        Boolean.TRUE.equals(
                                optionRequest.getCorrect()
                        )
                );


                question.addOption(
                        option
                );
            }
        }


        Quiz saved =
                quizRepository.save(quiz);


        return toResponse(saved);
    }


    /* =========================
       LIST TRAINER QUIZZES
    ========================= */

    @Transactional(readOnly = true)
    public List<QuizResponse> getTrainerQuizzes(
            String trainerEmail
    ) {

        User trainer =
                getTrainer(trainerEmail);


        return quizRepository
                .findAll()
                .stream()
                .filter(quiz ->
                        quiz.getBatch()
                                .getTrainer()
                                != null
                )
                .filter(quiz ->
                        quiz.getBatch()
                                .getTrainer()
                                .getId()
                                .equals(
                                        trainer.getId()
                                )
                )
                .map(this::toResponse)
                .toList();
    }


    /* =========================
       GET ONE QUIZ
    ========================= */

    @Transactional(readOnly = true)
    public QuizResponse getTrainerQuiz(
            String trainerEmail,
            Long quizId
    ) {

        User trainer =
                getTrainer(trainerEmail);


        Quiz quiz =
                getQuiz(quizId);


        verifyTrainerOwnsBatch(
                trainer,
                quiz.getBatch()
        );


        return toResponse(quiz);
    }


    /* =========================
       UPDATE QUIZ
    ========================= */

    @Transactional
    public QuizResponse updateQuiz(
            String trainerEmail,
            Long quizId,
            QuizCreateRequest request
    ) {

        User trainer =
                getTrainer(trainerEmail);


        Quiz quiz =
                getQuiz(quizId);


        verifyTrainerOwnsBatch(
                trainer,
                quiz.getBatch()
        );


        if (
                quiz.getStatus()
                        == QuizStatus.PUBLISHED
        ) {

            throw new IllegalArgumentException(
                    "Published quizzes cannot be edited"
            );
        }


        validateQuizRequest(request);


        Batch batch =
                batchRepository
                        .findById(
                                request.getBatchId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found"
                                )
                        );


        verifyTrainerOwnsBatch(
                trainer,
                batch
        );


        quiz.setTitle(
                request.getTitle().trim()
        );


        quiz.setTimeLimitMinutes(
                request.getTimeLimitMinutes()
        );


        quiz.setBatch(batch);


        /*
         * Replace existing questions.
         *
         * orphanRemoval=true means the old
         * questions/options are removed.
         */

        quiz.getQuestions().clear();


        for (
                QuestionRequest questionRequest
                : request.getQuestions()
        ) {

            validateQuestion(
                    questionRequest
            );


            Question question =
                    new Question();


            question.setQuestionText(
                    questionRequest
                            .getQuestionText()
                            .trim()
            );


            question.setType(
                    questionRequest.getType()
            );


            quiz.addQuestion(
                    question
            );


            for (
                    OptionRequest optionRequest
                    : questionRequest.getOptions()
            ) {

                Option option =
                        new Option();


                option.setOptionText(
                        optionRequest
                                .getOptionText()
                                .trim()
                );


                option.setCorrect(
                        Boolean.TRUE.equals(
                                optionRequest.getCorrect()
                        )
                );


                question.addOption(
                        option
                );
            }
        }


        return toResponse(
                quizRepository.save(quiz)
        );
    }


    /* =========================
       PUBLISH QUIZ
    ========================= */

    @Transactional
    public QuizResponse publishQuiz(
            String trainerEmail,
            Long quizId
    ) {

        User trainer =
                getTrainer(trainerEmail);


        Quiz quiz =
                getQuiz(quizId);


        verifyTrainerOwnsBatch(
                trainer,
                quiz.getBatch()
        );


        if (
                quiz.getStatus()
                        == QuizStatus.PUBLISHED
        ) {

            throw new IllegalArgumentException(
                    "Quiz is already published"
            );
        }


        /*
         * Validate again before publishing.
         *
         * This protects us even if a future
         * frontend bug bypasses validation.
         */

        validateQuizEntity(
                quiz
        );


        quiz.setStatus(
                QuizStatus.PUBLISHED
        );


        return toResponse(
                quizRepository.save(quiz)
        );
    }


    /* =========================
       DELETE QUIZ
    ========================= */

    @Transactional
    public void deleteQuiz(
            String trainerEmail,
            Long quizId
    ) {

        User trainer =
                getTrainer(trainerEmail);


        Quiz quiz =
                getQuiz(quizId);


        verifyTrainerOwnsBatch(
                trainer,
                quiz.getBatch()
        );


        if (
                quiz.getStatus()
                        == QuizStatus.PUBLISHED
        ) {

            throw new IllegalArgumentException(
                    "Published quizzes cannot be deleted"
            );
        }


        quizRepository.delete(
                quiz
        );
    }


    /* =========================
       VALIDATE QUIZ REQUEST
    ========================= */

    private void validateQuizRequest(
            QuizCreateRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Quiz data is required"
            );
        }


        if (
                request.getTitle() == null
                        ||
                        request.getTitle()
                                .trim()
                                .isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Quiz title is required"
            );
        }


        if (
                request.getTitle()
                        .trim()
                        .length() > 150
        ) {

            throw new IllegalArgumentException(
                    "Quiz title must not exceed 150 characters"
            );
        }


        if (
                request.getTimeLimitMinutes()
                        == null
                        ||
                        request.getTimeLimitMinutes()
                                <= 0
        ) {

            throw new IllegalArgumentException(
                    "Time limit must be greater than zero"
            );
        }


        if (
                request.getBatchId()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Batch is required"
            );
        }


        if (
                request.getQuestions()
                        == null
                        ||
                        request.getQuestions()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Quiz must contain at least one question"
            );
        }


        for (
                QuestionRequest question
                : request.getQuestions()
        ) {

            validateQuestion(
                    question
            );
        }
    }


    /* =========================
       VALIDATE QUESTION
    ========================= */

    private void validateQuestion(
            QuestionRequest question
    ) {

        if (question == null) {

            throw new IllegalArgumentException(
                    "Question cannot be empty"
            );
        }


        if (
                question.getQuestionText()
                        == null
                        ||
                        question.getQuestionText()
                                .trim()
                                .isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Question text is required"
            );
        }


        if (
                question.getType()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Question type is required"
            );
        }


        if (
                question.getOptions()
                        == null
                        ||
                        question.getOptions().size() < 3
        ) {

            throw new IllegalArgumentException(
                    "Each question must have at least 3 options"
            );
        }


        if (
                question.getOptions().size() > 6
        ) {

            throw new IllegalArgumentException(
                    "Each question cannot have more than 6 options"
            );
        }


        int correctCount = 0;


        for (
                OptionRequest option
                : question.getOptions()
        ) {

            if (
                    option == null
                            ||
                            option.getOptionText()
                                    == null
                            ||
                            option.getOptionText()
                                    .trim()
                                    .isBlank()
            ) {

                throw new IllegalArgumentException(
                        "Option text cannot be empty"
                );
            }


            if (
                    Boolean.TRUE.equals(
                            option.getCorrect()
                    )
            ) {

                correctCount++;
            }
        }


        if (correctCount == 0) {

            throw new IllegalArgumentException(
                    "At least one correct option is required"
            );
        }


        if (
                question.getType()
                        == QuestionType.SINGLE_CORRECT
                        &&
                        correctCount != 1
        ) {

            throw new IllegalArgumentException(
                    "Single correct questions must have exactly one correct option"
            );
        }
    }


    /* =========================
       VALIDATE SAVED QUIZ
    ========================= */

    private void validateQuizEntity(
            Quiz quiz
    ) {

        if (
                quiz.getTitle() == null
                        ||
                        quiz.getTitle()
                                .isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Quiz title is required"
            );
        }


        if (
                quiz.getTimeLimitMinutes()
                        == null
                        ||
                        quiz.getTimeLimitMinutes()
                                <= 0
        ) {

            throw new IllegalArgumentException(
                    "Invalid time limit"
            );
        }


        if (
                quiz.getQuestions()
                        == null
                        ||
                        quiz.getQuestions()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Quiz must contain at least one question"
            );
        }


        for (
                Question question
                : quiz.getQuestions()
        ) {

            if (
                    question.getOptions()
                            == null
                            ||
                            question.getOptions().size()
                                    < 3
            ) {

                throw new IllegalArgumentException(
                        "Every question must have at least 3 options"
                );
            }


            if (
                    question.getOptions().size()
                            > 6
            ) {

                throw new IllegalArgumentException(
                        "Every question can have at most 6 options"
                );
            }


            long correctCount =
                    question.getOptions()
                            .stream()
                            .filter(option ->
                                    Boolean.TRUE.equals(
                                            option.getCorrect()
                                    )
                            )
                            .count();


            if (correctCount == 0) {

                throw new IllegalArgumentException(
                        "Every question must have a correct answer"
                );
            }


            if (
                    question.getType()
                            == QuestionType.SINGLE_CORRECT
                            &&
                            correctCount != 1
            ) {

                throw new IllegalArgumentException(
                        "Single correct questions must have exactly one correct answer"
                );
            }
        }
    }


    /* =========================
       GET TRAINER
    ========================= */

    private User getTrainer(
            String email
    ) {

        User trainer =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trainer not found"
                                )
                        );


        if (
                trainer.getRole()
                        != Role.TRAINER
        ) {

            throw new IllegalArgumentException(
                    "Only trainers can manage quizzes"
            );
        }


        return trainer;
    }


    /* =========================
       GET QUIZ
    ========================= */

    private Quiz getQuiz(
            Long quizId
    ) {

        return quizRepository
                .findById(quizId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Quiz not found"
                        )
                );
    }


    /* =========================
       VERIFY TRAINER
    ========================= */

    private void verifyTrainerOwnsBatch(
            User trainer,
            Batch batch
    ) {

        if (
                batch.getTrainer() == null
        ) {

            throw new IllegalArgumentException(
                    "No trainer is assigned to this batch"
            );
        }


        if (
                !batch.getTrainer()
                        .getId()
                        .equals(
                                trainer.getId()
                        )
        ) {

            throw new IllegalArgumentException(
                    "You are not assigned to this batch"
            );
        }
    }


    /* =========================
       RESPONSE MAPPING
    ========================= */

    private QuizResponse toResponse(
            Quiz quiz
    ) {

        List<QuestionResponse>
                questions =
                quiz.getQuestions()
                        .stream()
                        .map(question -> {

                            List<OptionResponse>
                                    options =
                                    question
                                            .getOptions()
                                            .stream()
                                            .map(option ->
                                                    new OptionResponse(
                                                            option.getId(),
                                                            option.getOptionText()
                                                    )
                                            )
                                            .toList();


                            return new QuestionResponse(
                                    question.getId(),
                                    question.getQuestionText(),
                                    question.getType(),
                                    options
                            );
                        })
                        .toList();


        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTimeLimitMinutes(),
                quiz.getStatus(),
                quiz.getBatch().getId(),
                quiz.getBatch().getName(),
                quiz.getCreatedAt(),
                questions
        );
    }
}