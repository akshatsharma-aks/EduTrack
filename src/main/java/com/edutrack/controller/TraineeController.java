package com.edutrack.controller;

import com.edutrack.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trainee")
public class TraineeController {

    private final EnrollmentService enrollmentService;

    public TraineeController(
            EnrollmentService enrollmentService
    ) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/batches")
    public ResponseEntity<?> availableBatches() {

        return ResponseEntity.ok(
                enrollmentService
                        .getAvailableBatches()
        );
    }

    @PostMapping("/batches/{batchId}/request")
    public ResponseEntity<?> requestToJoin(
            @PathVariable Long batchId,
            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        enrollmentService.requestToJoin(
                                authentication.getName(),
                                batchId
                        )
                );
    }

    @GetMapping("/enrollments")
    public ResponseEntity<?> myEnrollments(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                enrollmentService
                        .getMyEnrollments(
                                authentication.getName()
                        )
        );
    }

    @GetMapping("/my-batches")
    public ResponseEntity<?> myApprovedBatches(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                enrollmentService
                        .getApprovedBatches(
                                authentication.getName()
                        )
        );
    }
}

