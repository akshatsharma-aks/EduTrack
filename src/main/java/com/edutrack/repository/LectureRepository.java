package com.edutrack.repository;

import com.edutrack.entity.Batch;
import com.edutrack.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository
        extends JpaRepository<Lecture, Long> {

    List<Lecture> findByBatchOrderByUploadedAtAsc(
            Batch batch
    );
}