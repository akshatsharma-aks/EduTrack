package com.edutrack.repository;

import com.edutrack.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository
        extends JpaRepository<Course, Long> {

    long countByStatus(String status);
}