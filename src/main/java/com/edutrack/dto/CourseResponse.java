package com.edutrack.dto;

public class CourseResponse {

    private Long id;
    private String name;
    private String description;
    private String status;

    public CourseResponse(
            Long id,
            String name,
            String description,
            String status
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
}