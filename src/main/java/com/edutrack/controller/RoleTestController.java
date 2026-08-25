package com.edutrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleTestController {

    @GetMapping("/admin/test")
    public String adminTest() {
        return "ADMIN access granted";
    }

    @GetMapping("/trainer/test")
    public String trainerTest() {
        return "TRAINER access granted";
    }

    @GetMapping("/trainee/test")
    public String traineeTest() {
        return "TRAINEE access granted";
    }
}