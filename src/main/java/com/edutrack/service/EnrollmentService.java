package com.edutrack.service;

import com.edutrack.dto.AvailableBatchResponse;
import com.edutrack.dto.EnrollmentRequestResponse;
import com.edutrack.dto.EnrollmentResponse;
import com.edutrack.entity.Batch;
import com.edutrack.entity.Enrollment;
import com.edutrack.entity.User;
import com.edutrack.enums.EnrollmentStatus;
import com.edutrack.enums.Role;
import com.edutrack.repository.BatchRepository;
import com.edutrack.repository.EnrollmentRepository;
import com.edutrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final BatchRepository batchRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            BatchRepository batchRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
    }

    /*
     * TRAINEE
     * View batches available for enrollment.
     */
    @Transactional(readOnly = true)
    public List<AvailableBatchResponse> getAvailableBatches() {

        return batchRepository.findAll()
                .stream()
                .map(batch -> {

                    User trainer = batch.getTrainer();

                    return new AvailableBatchResponse(
                            batch.getId(),
                            batch.getName(),
                            batch.getCourse().getName(),
                            batch.getStartDate(),
                            batch.getEndDate(),
                            trainer != null
                                    ? trainer.getName()
                                    : "Not allocated"
                    );
                })
                .toList();
    }

    /*
     * TRAINEE
     * Request to join a batch.
     */
    @Transactional
    public EnrollmentResponse requestToJoin(
            String traineeEmail,
            Long batchId
    ) {

        User trainee =
                getTraineeByEmail(traineeEmail);

        Batch batch =
                batchRepository
                        .findById(batchId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found"
                                )
                        );

        if (batch.getTrainer() == null) {

            throw new IllegalArgumentException(
                    "This batch does not have a trainer allocated yet"
            );
        }

        if (enrollmentRepository
                .existsByTraineeAndBatch(
                        trainee,
                        batch
                )) {

            throw new IllegalArgumentException(
                    "You have already requested this batch"
            );
        }

        Enrollment enrollment =
                new Enrollment();

        enrollment.setTrainee(trainee);
        enrollment.setBatch(batch);
        enrollment.setStatus(
                EnrollmentStatus.PENDING
        );

        Enrollment saved =
                enrollmentRepository.save(enrollment);

        return toEnrollmentResponse(saved);
    }

    /*
     * TRAINEE
     * View own enrollment requests / memberships.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(
            String traineeEmail
    ) {

        User trainee =
                getTraineeByEmail(traineeEmail);

        return enrollmentRepository
                .findByTrainee(trainee)
                .stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    /*
     * TRAINER
     * View pending requests for batches assigned
     * to the logged-in trainer.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentRequestResponse>
    getTrainerPendingRequests(
            String trainerEmail
    ) {

        User trainer =
                getTrainerByEmail(trainerEmail);

        return enrollmentRepository
                .findAll()
                .stream()
                .filter(enrollment ->
                        enrollment
                                .getBatch()
                                .getTrainer()
                                .getId()
                                .equals(trainer.getId())
                )
                .filter(enrollment ->
                        enrollment.getStatus()
                                == EnrollmentStatus.PENDING
                )
                .map(this::toEnrollmentRequestResponse)
                .toList();
    }

    /*
     * TRAINER
     * Approve trainee enrollment.
     */
    @Transactional
    public EnrollmentRequestResponse approveEnrollment(
            String trainerEmail,
            Long enrollmentId
    ) {

        User trainer =
                getTrainerByEmail(trainerEmail);

        Enrollment enrollment =
                getEnrollment(enrollmentId);

        verifyTrainerOwnsEnrollment(
                trainer,
                enrollment
        );

        if (enrollment.getStatus()
                != EnrollmentStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Only pending requests can be approved"
            );
        }

        enrollment.setStatus(
                EnrollmentStatus.APPROVED
        );

        Enrollment saved =
                enrollmentRepository.save(enrollment);

        return toEnrollmentRequestResponse(saved);
    }

    /*
     * TRAINER
     * Reject trainee enrollment.
     */
    @Transactional
    public EnrollmentRequestResponse rejectEnrollment(
            String trainerEmail,
            Long enrollmentId
    ) {

        User trainer =
                getTrainerByEmail(trainerEmail);

        Enrollment enrollment =
                getEnrollment(enrollmentId);

        verifyTrainerOwnsEnrollment(
                trainer,
                enrollment
        );

        if (enrollment.getStatus()
                != EnrollmentStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Only pending requests can be rejected"
            );
        }

        enrollment.setStatus(
                EnrollmentStatus.REJECTED
        );

        Enrollment saved =
                enrollmentRepository.save(enrollment);

        return toEnrollmentRequestResponse(saved);
    }

    /*
     * APPROVED TRAINEE ONLY
     * Returns batches the trainee can actually access.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse>
    getApprovedBatches(
            String traineeEmail
    ) {

        User trainee =
                getTraineeByEmail(traineeEmail);

        return enrollmentRepository
                .findByTrainee(trainee)
                .stream()
                .filter(enrollment ->
                        enrollment.getStatus()
                                == EnrollmentStatus.APPROVED
                )
                .map(this::toEnrollmentResponse)
                .toList();
    }

    private User getTraineeByEmail(
            String email
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        if (user.getRole() != Role.TRAINEE) {

            throw new IllegalArgumentException(
                    "Only trainees can perform this action"
            );
        }

        return user;
    }

    private User getTrainerByEmail(
            String email
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        if (user.getRole() != Role.TRAINER) {

            throw new IllegalArgumentException(
                    "Only trainers can perform this action"
            );
        }

        return user;
    }

    private Enrollment getEnrollment(
            Long enrollmentId
    ) {

        return enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enrollment request not found"
                        )
                );
    }

    private void verifyTrainerOwnsEnrollment(
            User trainer,
            Enrollment enrollment
    ) {

        User assignedTrainer =
                enrollment
                        .getBatch()
                        .getTrainer();

        if (assignedTrainer == null
                || !assignedTrainer.getId()
                .equals(trainer.getId())) {

            throw new IllegalArgumentException(
                    "You are not authorized to manage this enrollment"
            );
        }
    }

    private EnrollmentResponse toEnrollmentResponse(
            Enrollment enrollment
    ) {

        Batch batch =
                enrollment.getBatch();

        return new EnrollmentResponse(
                enrollment.getId(),
                batch.getId(),
                batch.getName(),
                batch.getCourse().getName(),
                batch.getStartDate(),
                batch.getEndDate(),
                enrollment.getStatus(),
                enrollment.getRequestedAt()
        );
    }

    private EnrollmentRequestResponse
    toEnrollmentRequestResponse(
            Enrollment enrollment
    ) {

        Batch batch =
                enrollment.getBatch();

        User trainee =
                enrollment.getTrainee();

        return new EnrollmentRequestResponse(
                enrollment.getId(),
                trainee.getId(),
                trainee.getName(),
                trainee.getEmail(),
                batch.getId(),
                batch.getName(),
                batch.getCourse().getName(),
                batch.getStartDate(),
                batch.getEndDate(),
                enrollment.getStatus()
        );
    }
}

