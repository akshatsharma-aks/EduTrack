package com.edutrack.service;

import com.edutrack.dto.TrainerProgressResponse;
import com.edutrack.dto.VideoProgressRequest;
import com.edutrack.dto.VideoProgressResponse;
import com.edutrack.entity.Enrollment;
import com.edutrack.entity.Lecture;
import com.edutrack.entity.User;
import com.edutrack.entity.VideoProgress;
import com.edutrack.enums.EnrollmentStatus;
import com.edutrack.enums.Role;
import com.edutrack.repository.EnrollmentRepository;
import com.edutrack.repository.LectureRepository;
import com.edutrack.repository.UserRepository;
import com.edutrack.repository.VideoProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VideoProgressService {

    private static final double
            COMPLETION_THRESHOLD = 95.0;


    private final VideoProgressRepository
            videoProgressRepository;

    private final UserRepository
            userRepository;

    private final LectureRepository
            lectureRepository;

    private final EnrollmentRepository
            enrollmentRepository;


    public VideoProgressService(
            VideoProgressRepository videoProgressRepository,
            UserRepository userRepository,
            LectureRepository lectureRepository,
            EnrollmentRepository enrollmentRepository
    ) {

        this.videoProgressRepository =
                videoProgressRepository;

        this.userRepository =
                userRepository;

        this.lectureRepository =
                lectureRepository;

        this.enrollmentRepository =
                enrollmentRepository;
    }


    @Transactional(readOnly = true)
    public VideoProgressResponse
    getProgress(
            String traineeEmail,
            Long lectureId
    ) {

        User trainee =
                getTrainee(traineeEmail);

        Lecture lecture =
                getLecture(lectureId);

        verifyApprovedEnrollment(
                trainee,
                lecture
        );


        return videoProgressRepository
                .findByTraineeAndLecture(
                        trainee,
                        lecture
                )
                .map(this::toResponse)
                .orElse(
                        new VideoProgressResponse(
                                lectureId,
                                0.0,
                                0.0,
                                false,
                                null
                        )
                );
    }


    @Transactional
    public VideoProgressResponse
    saveProgress(
            String traineeEmail,
            Long lectureId,
            VideoProgressRequest request
    ) {

        User trainee =
                getTrainee(traineeEmail);

        Lecture lecture =
                getLecture(lectureId);

        verifyApprovedEnrollment(
                trainee,
                lecture
        );


        if (request == null
                || request.getCurrentPosition() == null
                || request.getDuration() == null) {

            throw new IllegalArgumentException(
                    "Current position and duration are required"
            );
        }


        double currentPosition =
                request.getCurrentPosition();

        double duration =
                request.getDuration();


        if (duration <= 0) {

            throw new IllegalArgumentException(
                    "Video duration must be greater than zero"
            );
        }


        if (currentPosition < 0) {

            currentPosition = 0;
        }


        if (currentPosition > duration) {

            currentPosition = duration;
        }


        double percentage =
                (currentPosition / duration) * 100.0;


        percentage =
                Math.min(
                        100.0,
                        Math.max(
                                0.0,
                                percentage
                        )
                );


        /*
         * Completion is decided ONLY
         * by the backend.
         *
         * Opening the video creates
         * no progress record.
         *
         * Completion requires >= 95%.
         */

        boolean completed =
                percentage >=
                        COMPLETION_THRESHOLD;


        VideoProgress progress =
                videoProgressRepository
                        .findByTraineeAndLecture(
                                trainee,
                                lecture
                        )
                        .orElseGet(() -> {

                            VideoProgress newProgress =
                                    new VideoProgress();

                            newProgress.setTrainee(
                                    trainee
                            );

                            newProgress.setLecture(
                                    lecture
                            );

                            return newProgress;
                        });


        progress.setCurrentPosition(
                currentPosition
        );


        /*
         * Never reduce previously saved
         * progress.
         */

        double previousPercentage =
                progress.getPercentageWatched() == null
                        ? 0.0
                        : progress.getPercentageWatched();


        progress.setPercentageWatched(
                Math.max(
                        previousPercentage,
                        percentage
                )
        );


        /*
         * Once completed, remain completed.
         */

        progress.setCompleted(
                Boolean.TRUE.equals(
                        progress.getCompleted()
                ) || completed
        );


        progress.setLastWatchedAt(
                LocalDateTime.now()
        );


        VideoProgress saved =
                videoProgressRepository.save(
                        progress
                );


        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<TrainerProgressResponse>
    getTrainerLectureProgress(
            String trainerEmail,
            Long lectureId
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
                    "Only trainers can view progress"
            );
        }


        Lecture lecture =
                getLecture(lectureId);


        if (
                lecture.getBatch().getTrainer() == null
                        ||
                        !lecture.getBatch()
                                .getTrainer()
                                .getId()
                                .equals(trainer.getId())
        ) {

            throw new IllegalArgumentException(
                    "You are not assigned to this batch"
            );
        }


        return videoProgressRepository
                .findByLecture(lecture)
                .stream()
                .map(progress ->
                        new TrainerProgressResponse(
                                progress.getTrainee().getId(),
                                progress.getTrainee().getName(),
                                progress.getTrainee().getEmail(),
                                lecture.getId(),
                                lecture.getTitle(),
                                progress.getPercentageWatched(),
                                progress.getCompleted()
                        )
                )
                .toList();
    }


    private User getTrainee(
            String email
    ) {

        User trainee =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trainee not found"
                                )
                        );


        if (trainee.getRole()
                != Role.TRAINEE) {

            throw new IllegalArgumentException(
                    "Only trainees can track progress"
            );
        }


        return trainee;
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


    private void verifyApprovedEnrollment(
            User trainee,
            Lecture lecture
    ) {

        Enrollment enrollment =
                enrollmentRepository
                        .findByTraineeAndBatch(
                                trainee,
                                lecture.getBatch()
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


    private VideoProgressResponse
    toResponse(
            VideoProgress progress
    ) {

        return new VideoProgressResponse(
                progress.getLecture().getId(),
                progress.getCurrentPosition(),
                progress.getPercentageWatched(),
                progress.getCompleted(),
                progress.getLastWatchedAt()
        );
    }
}