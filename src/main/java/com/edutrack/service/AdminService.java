package com.edutrack.service;

import com.edutrack.dto.*;
import com.edutrack.entity.Batch;
import com.edutrack.entity.Course;
import com.edutrack.entity.User;
import com.edutrack.enums.Role;
import com.edutrack.enums.TrainerStatus;
import com.edutrack.repository.BatchRepository;
import com.edutrack.repository.CourseRepository;
import com.edutrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;

    public AdminService(
            UserRepository userRepository,
            CourseRepository courseRepository,
            BatchRepository batchRepository
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.batchRepository = batchRepository;
    }

    public List<TrainerResponse> getTrainerRequests() {

        return userRepository
                .findAll()
                .stream()
                .filter(user ->
                        user.getRole() == Role.TRAINER)
                .map(user ->
                        new TrainerResponse(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getTrainerStatus()
                        )
                )
                .toList();
    }

    @Transactional
    public TrainerResponse approveTrainer(Long id) {

        User trainer = getTrainer(id);

        trainer.setTrainerStatus(
                TrainerStatus.APPROVED
        );

        trainer.setEnabled(true);

        userRepository.save(trainer);

        return toTrainerResponse(trainer);
    }

    @Transactional
    public TrainerResponse declineTrainer(Long id) {

        User trainer = getTrainer(id);

        trainer.setTrainerStatus(
                TrainerStatus.DECLINED
        );

        trainer.setEnabled(false);

        userRepository.save(trainer);

        return toTrainerResponse(trainer);
    }

    public Course createCourse(
            CourseRequest request
    ) {

        Course course = new Course();

        course.setName(
                request.getName().trim()
        );

        course.setDescription(
                request.getDescription().trim()
        );

        course.setStatus(
                request.getStatus().toUpperCase()
        );

        return courseRepository.save(course);
    }

    public List<CourseResponse> getCourses() {

        return courseRepository.findAll()
                .stream()
                .map(course ->
                        new CourseResponse(
                                course.getId(),
                                course.getName(),
                                course.getDescription(),
                                course.getStatus()
                        )
                )
                .toList();
    }

//    @Transactional
//    public BatchResponse createBatch(
//            BatchRequest request
//    ) {
//
//        if (request.getEndDate()
//                .isBefore(request.getStartDate())) {
//
//            throw new IllegalArgumentException(
//                    "End date cannot be before start date"
//            );
//        }
//
//        Course course =
//                courseRepository
//                        .findById(request.getCourseId())
//                        .orElseThrow(() ->
//                                new IllegalArgumentException(
//                                        "Course not found"
//                                )
//                        );
//
//        Batch batch = new Batch();
//
//        batch.setName(
//                request.getName().trim()
//        );
//
//        batch.setCourse(course);
//
//        batch.setStartDate(
//                request.getStartDate()
//        );
//
//        batch.setEndDate(
//                request.getEndDate()
//        );
//
//        if (request.getTrainerId() != null) {
//
//            User trainer =
//                    getApprovedTrainer(
//                            request.getTrainerId()
//                    );
//
//            batch.setTrainer(trainer);
//        }
//
//        return batchRepository.save(batch);
//    }
@Transactional
public BatchResponse createBatch(
        BatchRequest request
) {

    if (request.getEndDate()
            .isBefore(request.getStartDate())) {

        throw new IllegalArgumentException(
                "End date cannot be before start date"
        );
    }

    Course course =
            courseRepository
                    .findById(request.getCourseId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course not found"
                            )
                    );

    Batch batch = new Batch();

    batch.setName(
            request.getName().trim()
    );

    batch.setCourse(course);

    batch.setStartDate(
            request.getStartDate()
    );

    batch.setEndDate(
            request.getEndDate()
    );

    if (request.getTrainerId() != null) {

        User trainer =
                getApprovedTrainer(
                        request.getTrainerId()
                );

        batch.setTrainer(trainer);
    }

    Batch savedBatch =
            batchRepository.save(batch);

    User trainer =
            savedBatch.getTrainer();

    return new BatchResponse(
            savedBatch.getId(),
            savedBatch.getName(),
            savedBatch.getCourse().getId(),
            savedBatch.getCourse().getName(),
            savedBatch.getStartDate(),
            savedBatch.getEndDate(),
            trainer != null
                    ? trainer.getId()
                    : null,
            trainer != null
                    ? trainer.getName()
                    : null
    );
}

    public List<BatchResponse> getBatches() {

        return batchRepository.findAll()
                .stream()
                .map(batch -> {

                    User trainer = batch.getTrainer();

                    return new BatchResponse(
                            batch.getId(),
                            batch.getName(),
                            batch.getCourse().getId(),
                            batch.getCourse().getName(),
                            batch.getStartDate(),
                            batch.getEndDate(),
                            trainer != null
                                    ? trainer.getId()
                                    : null,
                            trainer != null
                                    ? trainer.getName()
                                    : null
                    );
                })
                .toList();
    }

    public void allocateTrainer(
            Long batchId,
            Long trainerId
    ) {

        Batch batch =
                batchRepository
                        .findById(batchId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found"
                                )
                        );

        User trainer =
                getApprovedTrainer(trainerId);

        batch.setTrainer(trainer);

        batchRepository.save(batch);
    }

    public long getCourseCount() {

        return courseRepository.count();
    }

    public long getActiveBatchCount() {

        return batchRepository
                .countByEndDateGreaterThanEqual(
                        LocalDate.now()
                );
    }

    public long getTrainerCount() {

        return userRepository.countByRoleAndTrainerStatus(
                Role.TRAINER,
                TrainerStatus.APPROVED
        );
    }

    public long getTraineeCount() {

        return userRepository.countByRole(
                Role.TRAINEE
        );
    }

    public List<Batch> getTrainerBatches(
            String email
    ) {

        User trainer =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return batchRepository
                .findByTrainer(trainer);
    }

    private User getTrainer(Long id) {

        User trainer =
                userRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trainer not found"
                                )
                        );

        if (trainer.getRole() != Role.TRAINER) {

            throw new IllegalArgumentException(
                    "Selected user is not a trainer"
            );
        }

        return trainer;
    }

    private User getApprovedTrainer(Long id) {

        User trainer = getTrainer(id);

        if (trainer.getTrainerStatus()
                != TrainerStatus.APPROVED) {

            throw new IllegalArgumentException(
                    "Only approved trainers can be allocated"
            );
        }

        return trainer;
    }

    private TrainerResponse toTrainerResponse(
            User trainer
    ) {

        return new TrainerResponse(
                trainer.getId(),
                trainer.getName(),
                trainer.getEmail(),
                trainer.getTrainerStatus()
        );
    }
}
