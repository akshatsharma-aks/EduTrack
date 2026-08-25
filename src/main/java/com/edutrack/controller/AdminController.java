package com.edutrack.controller;

import com.edutrack.dto.BatchRequest;
import com.edutrack.dto.CourseRequest;
import com.edutrack.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(
            AdminService adminService
    ) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {

        return ResponseEntity.ok(
                Map.of(
                        "totalCourses",
                        adminService.getCourseCount(),

                        "activeBatches",
                        adminService.getActiveBatchCount(),

                        "trainers",
                        adminService.getTrainerCount(),

                        "trainees",
                        adminService.getTraineeCount()
                )
        );
    }

    @GetMapping("/trainers")
    public ResponseEntity<?> trainers() {

        return ResponseEntity.ok(
                adminService.getTrainerRequests()
        );
    }

    @PutMapping("/trainers/{id}/approve")
    public ResponseEntity<?> approveTrainer(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                adminService.approveTrainer(id)
        );
    }

    @PutMapping("/trainers/{id}/decline")
    public ResponseEntity<?> declineTrainer(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                adminService.declineTrainer(id)
        );
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CourseRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminService.createCourse(request)
                );
    }

    @GetMapping("/courses")
    public ResponseEntity<?> courses() {

        return ResponseEntity.ok(
                adminService.getCourses()
        );
    }

    @PostMapping("/batches")
    public ResponseEntity<?> createBatch(
            @Valid @RequestBody BatchRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminService.createBatch(request)
                );
    }

    @GetMapping("/batches")
    public ResponseEntity<?> batches() {

        return ResponseEntity.ok(
                adminService.getBatches()
        );
    }

    @PutMapping("/batches/{batchId}/trainer/{trainerId}")
    public ResponseEntity<?> allocateTrainer(
            @PathVariable Long batchId,
            @PathVariable Long trainerId
    ) {

        adminService.allocateTrainer(
                batchId,
                trainerId
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Trainer allocated successfully"
                )
        );
    }
}
