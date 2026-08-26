package com.edutrack.service;

import com.edutrack.dto.*;
import com.edutrack.entity.*;
import com.edutrack.enums.EnrollmentStatus;
import com.edutrack.enums.QuestionType;
import com.edutrack.enums.QuizStatus;
import com.edutrack.enums.Role;
import com.edutrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public QuizAttemptService(
            QuizRepository quizRepository,
            QuizAttemptRepository attemptRepository,
            AnswerRepository answerRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository
    ) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public QuizAttemptStartResponse startQuiz(
            String traineeEmail,
            Long quizId
    ) {

        User trainee = getTrainee(traineeEmail);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Quiz not found"
                        )
                );

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "This quiz is not published"
            );
        }

        verifyEnrollment(
                trainee,
                quiz.getBatch()
        );

        long previousAttempts =
                attemptRepository.countByQuizAndTrainee(
                        quiz,
                        trainee
                );

        QuizAttempt attempt =
                new QuizAttempt();

        attempt.setQuiz(quiz);
        attempt.setTrainee(trainee);
        attempt.setAttemptNumber(
                (int) previousAttempts + 1
        );
        attempt.setScore(0);
        attempt.setTotalQuestions(
                quiz.getQuestions().size()
        );
        attempt.setPercentage(0.0);
        attempt.setStartedAt(
                LocalDateTime.now()
        );

        QuizAttempt saved =
                attemptRepository.save(attempt);

        return new QuizAttemptStartResponse(
                saved.getId(),
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTimeLimitMinutes(),
                saved.getStartedAt()
                        .atZone(
                                java.time.ZoneId.systemDefault()
                        )
                        .toInstant()
                        .toEpochMilli(),
                toSafeQuizResponse(quiz)
        );
    }

    @Transactional
    public QuizResultResponse submitQuiz(
            String traineeEmail,
            QuizSubmitRequest request
    ) {

        User trainee =
                getTrainee(traineeEmail);

        if (request == null
                || request.getAttemptId() == null) {

            throw new IllegalArgumentException(
                    "Attempt ID is required"
            );
        }

        QuizAttempt attempt =
                attemptRepository.findById(
                        request.getAttemptId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Quiz attempt not found"
                        )
                );

        if (!attempt.getTrainee()
                .getId()
                .equals(trainee.getId())) {

            throw new IllegalArgumentException(
                    "This attempt does not belong to you"
            );
        }

        if (attempt.getSubmittedAt() != null) {

            throw new IllegalArgumentException(
                    "Quiz has already been submitted"
            );
        }

        Quiz quiz = attempt.getQuiz();

        /*
         * Server-side time validation.
         */
        LocalDateTime deadline =
                attempt.getStartedAt()
                        .plusMinutes(
                                quiz.getTimeLimitMinutes()
                        );

        if (LocalDateTime.now()
                .isAfter(deadline)) {

            /*
             * We still evaluate the submitted
             * answers, but the frontend should
             * normally submit automatically at
             * the timer reaching zero.
             */
        }

        List<QuizAnswerRequest> submittedAnswers =
                request.getAnswers() == null
                        ? List.of()
                        : request.getAnswers();

        Map<Long, Set<Long>> submittedMap =
                new HashMap<>();

        for (
                QuizAnswerRequest answer
                : submittedAnswers
        ) {

            if (answer == null
                    || answer.getQuestionId() == null) {
                continue;
            }

            Set<Long> optionIds =
                    answer.getSelectedOptionIds()
                            == null
                            ? new HashSet<>()
                            : new HashSet<>(
                            answer.getSelectedOptionIds()
                    );

            submittedMap.put(
                    answer.getQuestionId(),
                    optionIds
            );
        }

        int score = 0;

        /*
         * Remove any existing answers in case
         * this method is ever reused.
         */
        answerRepository.deleteAll(
                answerRepository.findByAttempt(attempt)
        );

        for (
                Question question
                : quiz.getQuestions()
        ) {

            Set<Long> selected =
                    submittedMap.getOrDefault(
                            question.getId(),
                            Collections.emptySet()
                    );

            Set<Long> correct =
                    question.getOptions()
                            .stream()
                            .filter(option ->
                                    Boolean.TRUE.equals(
                                            option.getCorrect()
                                    )
                            )
                            .map(Option::getId)
                            .collect(Collectors.toSet());

            boolean questionCorrect =
                    selected.equals(correct);

            if (questionCorrect) {
                score++;
            }

            for (Long optionId : selected) {

                Option selectedOption =
                        question.getOptions()
                                .stream()
                                .filter(option ->
                                        option.getId()
                                                .equals(optionId)
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Invalid option selected"
                                        )
                                );

                Answer answer =
                        new Answer();

                answer.setAttempt(attempt);
                answer.setQuestion(question);
                answer.setSelectedOption(
                        selectedOption
                );

                answerRepository.save(answer);
            }
        }

        int totalQuestions =
                quiz.getQuestions().size();

        double percentage =
                totalQuestions == 0
                        ? 0.0
                        : ((double) score
                           / totalQuestions) * 100.0;

        attempt.setScore(score);
        attempt.setTotalQuestions(
                totalQuestions
        );
        attempt.setPercentage(
                Math.round(
                        percentage * 100.0
                ) / 100.0
        );
        attempt.setSubmittedAt(
                LocalDateTime.now()
        );

        QuizAttempt saved =
                attemptRepository.save(attempt);

        return toResultResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<QuizResultResponse> getTraineeResults(
            String traineeEmail
    ) {

        User trainee =
                getTrainee(traineeEmail);

        return attemptRepository
                .findByTraineeOrderBySubmittedAtDesc(
                        trainee
                )
                .stream()
                .filter(attempt ->
                        attempt.getSubmittedAt() != null
                )
                .map(this::toResultResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResultResponse> getTrainerResults(
            String trainerEmail,
            Long quizId
    ) {

        User trainer =
                userRepository.findByEmail(
                        trainerEmail
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Trainer not found"
                        )
                );

        if (trainer.getRole() != Role.TRAINER) {
            throw new IllegalArgumentException(
                    "Only trainers can view quiz results"
            );
        }

        Quiz quiz =
                quizRepository.findById(quizId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Quiz not found"
                                )
                        );

        if (quiz.getBatch().getTrainer() == null
                || !quiz.getBatch()
                .getTrainer()
                .getId()
                .equals(trainer.getId())) {

            throw new IllegalArgumentException(
                    "You are not assigned to this quiz"
            );
        }

        return attemptRepository
                .findByQuizOrderBySubmittedAtDesc(
                        quiz
                )
                .stream()
                .filter(attempt ->
                        attempt.getSubmittedAt() != null
                )
                .map(this::toResultResponse)
                .toList();
    }

    private User getTrainee(
            String email
    ) {

        User trainee =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trainee not found"
                                )
                        );

        if (trainee.getRole() != Role.TRAINEE) {
            throw new IllegalArgumentException(
                    "Only trainees can attempt quizzes"
            );
        }

        return trainee;
    }

    private void verifyEnrollment(
            User trainee,
            Batch batch
    ) {

        Enrollment enrollment =
                enrollmentRepository
                        .findByTraineeAndBatch(
                                trainee,
                                batch
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "You are not enrolled in this batch"
                                )
                        );

        if (enrollment.getStatus()
                != EnrollmentStatus.APPROVED) {

            throw new IllegalArgumentException(
                    "Your enrollment is not approved"
            );
        }
    }

    /*
     * Correct answers are deliberately excluded.
     */
    private QuizResponse toSafeQuizResponse(
            Quiz quiz
    ) {

        List<QuestionResponse> questions =
                quiz.getQuestions()
                        .stream()
                        .map(question -> {

                            List<OptionResponse> options =
                                    question.getOptions()
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

    private QuizResultResponse toResultResponse(
            QuizAttempt attempt
    ) {

        return new QuizResultResponse(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getTrainee().getName(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                attempt.getPercentage(),
                attempt.getAttemptNumber(),
                attempt.getSubmittedAt()
        );
    }
}