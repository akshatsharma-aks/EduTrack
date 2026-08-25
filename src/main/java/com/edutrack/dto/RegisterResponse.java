package com.edutrack.dto;

import com.edutrack.enums.Role;

public class RegisterResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean enabled;

    public RegisterResponse(
            Long id,
            String name,
            String email,
            Role role,
            boolean enabled
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
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

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}