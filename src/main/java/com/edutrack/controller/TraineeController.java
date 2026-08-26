package com.edutrack.controller;

import com.edutrack.dto.VideoProgressRequest;
import com.edutrack.service.EnrollmentService;
import com.edutrack.service.LectureService;
import com.edutrack.service.VideoProgressService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainee")
public class TraineeController {

    private final EnrollmentService enrollmentService;
    private final LectureService lectureService;
    private final VideoProgressService videoProgressService;


    public TraineeController(
            EnrollmentService enrollmentService,
            LectureService lectureService,
            VideoProgressService videoProgressService
    ) {
        this.enrollmentService =
                enrollmentService;
        this.lectureService =
                lectureService;
        this.videoProgressService =
                videoProgressService;
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


    /*
     * =========================
     * TRAINEE LECTURES
     * =========================
     */

    @GetMapping("/batches/{batchId}/lectures")
    public ResponseEntity<?> getLectures(
            @PathVariable Long batchId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                lectureService.getTraineeLectures(
                        authentication.getName(),
                        batchId
                )
        );
    }


    /*
     * =========================
     * WATCH LECTURE VIDEO
     * =========================
     */

    @GetMapping("/lectures/{lectureId}/video")
    public ResponseEntity<Resource> watchLecture(
            @PathVariable Long lectureId,
            Authentication authentication
    ) {

        Resource resource =
                lectureService.getLectureVideo(
                        authentication.getName(),
                        lectureId
                );


        String contentType =
                lectureService.getLectureContentType(
                        lectureId
                );


        MediaType mediaType;


        try {

            mediaType =
                    MediaType.parseMediaType(
                            contentType
                    );

        } catch (Exception e) {

            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }


        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"lecture.mp4\""
                )
                .body(resource);
    }
    @GetMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<?> getLectureProgress(
            @PathVariable Long lectureId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                videoProgressService.getProgress(
                        authentication.getName(),
                        lectureId
                )
        );
    }


    @PutMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<?> saveLectureProgress(
            @PathVariable Long lectureId,
            @RequestBody VideoProgressRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                videoProgressService.saveProgress(
                        authentication.getName(),
                        lectureId,
                        request
                )
        );
    }
}