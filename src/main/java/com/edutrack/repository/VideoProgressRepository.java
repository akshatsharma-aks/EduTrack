package com.edutrack.repository;

import com.edutrack.entity.Lecture;
import com.edutrack.entity.User;
import com.edutrack.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoProgressRepository
        extends JpaRepository<VideoProgress, Long> {

    Optional<VideoProgress>
    findByTraineeAndLecture(
            User trainee,
            Lecture lecture
    );


    List<VideoProgress>
    findByTrainee(User trainee);


    List<VideoProgress>
    findByLecture(Lecture lecture);
}