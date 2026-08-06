package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}") // Retrieve user without his tasks
    public ResponseEntity<UserWithTaskDto> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')") // Only an ADMIN has access to this endpoint
    @GetMapping
    public ResponseEntity<List<UserWithTaskDto>> getAllUsers() { // TODO: paginate this
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
