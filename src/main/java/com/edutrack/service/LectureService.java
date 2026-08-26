package com.edutrack.service;

import com.edutrack.dto.LectureResponse;
import com.edutrack.entity.Batch;
import com.edutrack.entity.Enrollment;
import com.edutrack.entity.Lecture;
import com.edutrack.entity.User;
import com.edutrack.enums.EnrollmentStatus;
import com.edutrack.enums.Role;
import com.edutrack.repository.BatchRepository;
import com.edutrack.repository.EnrollmentRepository;
import com.edutrack.repository.LectureRepository;
import com.edutrack.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@Service
public class LectureService {

    private final LectureRepository lectureRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;

    public LectureService(
            LectureRepository lectureRepository,
            BatchRepository batchRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            FileStorageService fileStorageService
    ) {
        this.lectureRepository = lectureRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<LectureResponse> getTrainerLectures(
            String trainerEmail,
            Long batchId
    ) {

        Batch batch =
                getBatch(batchId);

        verifyTrainerOwnsBatch(
                trainerEmail,
                batch
        );

        return lectureRepository
                .findByBatchOrderByUploadedAtAsc(batch)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LectureResponse uploadLecture(
            String trainerEmail,
            Long batchId,
            String title,
            String description,
            MultipartFile file
    ) {

        Batch batch =
                getBatch(batchId);

        verifyTrainerOwnsBatch(
                trainerEmail,
                batch
        );

        if (title == null
                || title.trim().isBlank()) {

            throw new IllegalArgumentException(
                    "Lecture title is required"
            );
        }

        if (title.trim().length() > 150) {

            throw new IllegalArgumentException(
                    "Lecture title must not exceed 150 characters"
            );
        }

        FileStorageService.StoredFile storedFile =
                fileStorageService.store(file);

        try {

            Lecture lecture =
                    new Lecture();

            lecture.setTitle(
                    title.trim()
            );

            lecture.setDescription(
                    description == null
                            ? ""
                            : description.trim()
            );

            lecture.setOriginalFileName(
                    storedFile.originalFileName()
            );

            lecture.setStoredFileName(
                    storedFile.storedFileName()
            );

            lecture.setFilePath(
                    storedFile.filePath()
            );

            lecture.setContentType(
                    storedFile.contentType()
            );

            lecture.setFileSize(
                    storedFile.fileSize()
            );

            lecture.setBatch(batch);

            Lecture saved =
                    lectureRepository.save(lecture);

            return toResponse(saved);

        } catch (RuntimeException e) {

            fileStorageService.delete(
                    storedFile.filePath()
            );

            throw e;
        }
    }

    @Transactional
    public void deleteLecture(
            String trainerEmail,
            Long lectureId
    ) {

        Lecture lecture =
                getLecture(lectureId);

        verifyTrainerOwnsBatch(
                trainerEmail,
                lecture.getBatch()
        );

        String filePath =
                lecture.getFilePath();

        lectureRepository.delete(lecture);

        fileStorageService.delete(filePath);
    }

    @Transactional(readOnly = true)
    public List<LectureResponse> getTraineeLectures(
            String traineeEmail,
            Long batchId
    ) {

        Batch batch =
                getBatch(batchId);

        verifyApprovedTrainee(
                traineeEmail,
                batch
        );

        return lectureRepository
                .findByBatchOrderByUploadedAtAsc(batch)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource getLectureVideo(
            String traineeEmail,
            Long lectureId
    ) {

        Lecture lecture =
                getLecture(lectureId);

        verifyApprovedTrainee(
                traineeEmail,
                lecture.getBatch()
        );

        try {

            Path path =
                    fileStorageService.getPath(
                            lecture.getFilePath()
                    );

            Resource resource =
                    new UrlResource(
                            path.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new IllegalArgumentException(
                        "Video file is not available"
                );
            }

            return resource;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not load lecture video",
                    e
            );
        }
    }

    public String getLectureContentType(
            Long lectureId
    ) {

        return getLecture(lectureId)
                .getContentType();
    }

    private Batch getBatch(
            Long batchId
    ) {

        return batchRepository
                .findById(batchId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Batch not found"
                        )
                );
    }

    private Lecture getLecture(
            Long lectureId
    ) {

        return lectureRepository
                .findById(lectureId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Lecture not found"
                        )
                );
    }

    private void verifyTrainerOwnsBatch(
            String trainerEmail,
            Batch batch
    ) {

        User trainer =
                userRepository
                        .findByEmail(trainerEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trainer not found"
                                )
                        );

        if (trainer.getRole()
                != Role.TRAINER) {

            throw new IllegalArgumentException(
                    "Only trainers can manage lectures"
            );
        }

        if (batch.getTrainer() == null
                || !batch.getTrainer()
                .getId()
                .equals(trainer.getId())) {

            throw new IllegalArgumentException(
                    "You are not assigned to this batch"
            );
        }
    }

    private void verifyApprovedTrainee(
            String traineeEmail,
            Batch batch
    ) {

        User trainee =
                userRepository
                        .findByEmail(traineeEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trainee not found"
                                )
                        );

        if (trainee.getRole()
                != Role.TRAINEE) {

            throw new IllegalArgumentException(
                    "Only trainees can watch lectures"
            );
        }

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

    private LectureResponse toResponse(
            Lecture lecture
    ) {

        Batch batch =
                lecture.getBatch();

        return new LectureResponse(
                lecture.getId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getOriginalFileName(),
                lecture.getFileSize(),
                lecture.getUploadedAt(),
                batch.getId(),
                batch.getName()
        );
    }
}