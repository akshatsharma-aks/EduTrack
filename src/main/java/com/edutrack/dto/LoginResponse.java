package com.edutrack.dto;

import com.edutrack.enums.Role;

public class LoginResponse {

    private String token;
    private String name;
    private String email;
    private Role role;

    public LoginResponse(
            String token,
            String name,
            String email,
            Role role
    ) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
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
}