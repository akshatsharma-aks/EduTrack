package com.edutrack.controller;

import com.edutrack.dto.QuizSubmitRequest;
import com.edutrack.entity.Batch;
import com.edutrack.entity.Quiz;
import com.edutrack.entity.User;
import com.edutrack.enums.QuizStatus;
import com.edutrack.enums.Role;
import com.edutrack.repository.QuizRepository;
import com.edutrack.repository.UserRepository;
import com.edutrack.service.EnrollmentService;
import com.edutrack.service.QuizAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainee/quizzes")
public class TraineeQuizController {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final QuizAttemptService quizAttemptService;

    public TraineeQuizController(
            QuizRepository quizRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            QuizAttemptService quizAttemptService
    ) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.quizAttemptService = quizAttemptService;
    }

    @GetMapping
    public ResponseEntity<?> getPublishedQuizzes(
            Authentication authentication
    ) {

        User trainee =
                userRepository.findByEmail(
                        authentication.getName()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Trainee not found"
                        )
                );

        if (trainee.getRole() != Role.TRAINEE) {
            throw new IllegalArgumentException(
                    "Only trainees can access quizzes"
            );
        }

        List<Quiz> quizzes =
                quizRepository.findAll()
                        .stream()
                        .filter(quiz ->
                                quiz.getStatus()
                                        == QuizStatus.PUBLISHED
                        )
                        .filter(quiz ->
                                isApproved(
                                        trainee,
                                        quiz.getBatch()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(
                quizzes.stream()
                        .map(quiz ->
                                new com.edutrack.dto.QuizListResponse(
                                        quiz.getId(),
                                        quiz.getTitle(),
                                        quiz.getTimeLimitMinutes(),
                                        quiz.getStatus(),
                                        quiz.getBatch().getId(),
                                        quiz.getBatch().getName(),
                                        quiz.getBatch()
                                                .getCourse()
                                                .getName(),
                                        quiz.getQuestions()
                                                .size()
                                )
                        )
                        .toList()
        );
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuiz(
            @PathVariable Long quizId,
            Authentication authentication
    ) {

        /*
         * Starting the quiz returns the safe
         * question data and creates the attempt.
         */
        return ResponseEntity.ok(
                quizAttemptService.startQuiz(
                        authentication.getName(),
                        quizId
                )
        );
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(
            @RequestBody QuizSubmitRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                quizAttemptService.submitQuiz(
                        authentication.getName(),
                        request
                )
        );
    }

    @GetMapping("/results")
    public ResponseEntity<?> getResults(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                quizAttemptService.getTraineeResults(
                        authentication.getName()
                )
        );
    }

    private boolean isApproved(
            User trainee,
            Batch batch
    ) {

        try {

            return enrollmentService
                    .getApprovedBatches(
                            trainee.getEmail()
                    )
                    .stream()
                    .anyMatch(item ->
                            getBatchId(item)
                                    .equals(batch.getId())
                    );

        } catch (Exception e) {

            return false;
        }
    }

    private Long getBatchId(
            Object item
    ) {

        try {

            return (Long) item.getClass()
                    .getMethod("getBatchId")
                    .invoke(item);

        } catch (Exception e) {

            return -1L;
        }
    }
}