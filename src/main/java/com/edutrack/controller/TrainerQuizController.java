package com.edutrack.controller;

import com.edutrack.dto.QuizCreateRequest;
import com.edutrack.service.QuizAttemptService;
import com.edutrack.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer/quizzes")
public class TrainerQuizController {

    private final QuizAttemptService quizAttemptService;
    private final QuizService quizService;


    public TrainerQuizController(
            QuizService quizService,
            QuizAttemptService quizAttemptService
    ) {
        this.quizService = quizService;
        this.quizAttemptService = quizAttemptService;
    }


    /* =========================
       CREATE
    ========================= */

    @PostMapping
    public ResponseEntity<?> createQuiz(
            @RequestBody QuizCreateRequest request,
            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        quizService.createQuiz(
                                authentication.getName(),
                                request
                        )
                );
    }


    /* =========================
       LIST
    ========================= */

    @GetMapping
    public ResponseEntity<?> getQuizzes(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                quizService.getTrainerQuizzes(
                        authentication.getName()
                )
        );
    }


    /* =========================
       GET ONE
    ========================= */

    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuiz(
            @PathVariable Long quizId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                quizService.getTrainerQuiz(
                        authentication.getName(),
                        quizId
                )
        );
    }


    /* =========================
       UPDATE
    ========================= */

    @PutMapping("/{quizId}")
    public ResponseEntity<?> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizCreateRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                quizService.updateQuiz(
                        authentication.getName(),
                        quizId,
                        request
                )
        );
    }


    /* =========================
       PUBLISH
    ========================= */

    @PutMapping("/{quizId}/publish")
    public ResponseEntity<?> publishQuiz(
            @PathVariable Long quizId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                quizService.publishQuiz(
                        authentication.getName(),
                        quizId
                )
        );
    }


    /* =========================
       DELETE
    ========================= */

    @DeleteMapping("/{quizId}")
    public ResponseEntity<?> deleteQuiz(
            @PathVariable Long quizId,
            Authentication authentication
    ) {

        quizService.deleteQuiz(
                authentication.getName(),
                quizId
        );

        return ResponseEntity.noContent()
                .build();
    }
    @GetMapping("/{quizId}/results")
    public ResponseEntity<?> getQuizResults(
            @PathVariable Long quizId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                quizAttemptService.getTrainerResults(
                        authentication.getName(),
                        quizId
                )
        );
    }
}