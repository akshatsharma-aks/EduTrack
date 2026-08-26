package com.edutrack.controller;

import com.edutrack.dto.TrainerBatchResponse;
import com.edutrack.entity.Batch;
import com.edutrack.service.AdminService;
import com.edutrack.service.EnrollmentService;
import com.edutrack.service.LectureService;
import com.edutrack.service.VideoProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final EnrollmentService enrollmentService;
    private final AdminService adminService;
    private final LectureService lectureService;
    private final VideoProgressService videoProgressService;


    public TrainerController(
            AdminService adminService,
            EnrollmentService enrollmentService,
            LectureService lectureService,
            VideoProgressService videoProgressService
    ) {
        this.adminService = adminService;
        this.enrollmentService = enrollmentService;
        this.lectureService = lectureService;
        this.videoProgressService = videoProgressService;
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

    @GetMapping("/batches/{batchId}/lectures")
    public ResponseEntity<?> getLectures(
            @PathVariable Long batchId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                lectureService.getTrainerLectures(
                        authentication.getName(),
                        batchId
                )
        );
    }

    @PostMapping(
            value = "/batches/{batchId}/lectures",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadLecture(
            @PathVariable Long batchId,
            @RequestParam("title") String title,
            @RequestParam(
                    value = "description",
                    required = false
            )
            String description,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        lectureService.uploadLecture(
                                authentication.getName(),
                                batchId,
                                title,
                                description,
                                file
                        )
                );
    }

    @DeleteMapping("/lectures/{lectureId}")
    public ResponseEntity<?> deleteLecture(
            @PathVariable Long lectureId,
            Authentication authentication
    ) {

        lectureService.deleteLecture(
                authentication.getName(),
                lectureId
        );

        return ResponseEntity.noContent()
                .build();
    }
    @GetMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<?> getLectureProgress(
            @PathVariable Long lectureId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                videoProgressService
                        .getTrainerLectureProgress(
                                authentication.getName(),
                                lectureId
                        )
        );
    }
}