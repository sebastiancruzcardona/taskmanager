package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskWithUserDto;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        return new ResponseEntity<>(taskService.createTask(taskDto), HttpStatus.CREATED);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data") // consumes: tells the request from where to consume its data
    public ResponseEntity<String> uploadTasks(@RequestParam("file") MultipartFile file) {
        int created = taskService.createMultipleTasks(file);
        return ResponseEntity.ok("Uploaded " + created + " tasks");
    }

    @GetMapping
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @PageableDefault(page = 0, size = 5, sort = "title") Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/withUser/{id}")
    public ResponseEntity<TaskWithUserDto> getTaskWithUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTaskWithUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Integer id,
            @Valid @RequestBody TaskDto taskDto
    ) {
       return ResponseEntity.ok(taskService.updateTask(id, taskDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
