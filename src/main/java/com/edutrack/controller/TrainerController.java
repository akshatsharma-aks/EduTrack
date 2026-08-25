package com.edutrack.controller;

import com.edutrack.dto.TrainerBatchResponse;
import com.edutrack.entity.Batch;
import com.edutrack.service.AdminService;
import com.edutrack.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final EnrollmentService enrollmentService;
    private final AdminService adminService;

    public TrainerController(
            AdminService adminService,
            EnrollmentService enrollmentService
    ) {
        this.adminService = adminService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/batches")
    public List<TrainerBatchResponse> getAssignedBatches(
            Authentication authentication
    ) {

        return adminService
                .getTrainerBatches(authentication.getName())
                .stream()
                .map(batch ->
                        new TrainerBatchResponse(
                                batch.getId(),
                                batch.getName(),
                                batch.getCourse().getName(),
                                batch.getStartDate(),
                                batch.getEndDate()
                        )
                )
                .toList();
    }



    @GetMapping("/enrollment-requests")
    public ResponseEntity<?> enrollmentRequests(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                enrollmentService
                        .getTrainerPendingRequests(
                                authentication.getName()
                        )
        );
    }

    @PutMapping("/enrollment-requests/{id}/approve")
    public ResponseEntity<?> approveEnrollment(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                enrollmentService.approveEnrollment(
                        authentication.getName(),
                        id
                )
        );
    }

    @PutMapping("/enrollment-requests/{id}/reject")
    public ResponseEntity<?> rejectEnrollment(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                enrollmentService.rejectEnrollment(
                        authentication.getName(),
                        id
                )
        );
    }
}