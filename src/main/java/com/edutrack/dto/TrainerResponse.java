package com.edutrack.dto;

import com.edutrack.enums.TrainerStatus;

public class TrainerResponse {

    private Long id;
    private String name;
    private String email;
    private TrainerStatus status;

    public TrainerResponse(
            Long id,
            String name,
            String email,
            TrainerStatus status
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public TrainerStatus getStatus() {
        return status;
    }
}