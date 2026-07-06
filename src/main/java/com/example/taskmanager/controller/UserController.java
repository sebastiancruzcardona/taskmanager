package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}") // Retrieve user without his tasks
    public ResponseEntity<UserDto> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/withTasks/{id}") // Retrieve user with his tasks
    public ResponseEntity<UserWithTaskDto> getUserWithTasks(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserWithTasks(id));
    }
}
