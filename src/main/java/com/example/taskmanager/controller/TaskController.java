package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.exception.InvalidFileFormatException;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final Validator validator; // This also has a default value as the objectmapper

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        return new ResponseEntity<>(taskService.createTask(taskDto), HttpStatus.CREATED);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data") // consumes: tells the request from where to consume its data
    public ResponseEntity<String> uploadTask(@RequestParam("file") MultipartFile file) {
        // Transform into String
        try {
            // From file to String
            String json = new String(file.getBytes());

            // From String (JSON) to raw array to List<TaskDto>
            List<TaskDto> taskDtos = Arrays.asList(objectMapper.readValue(json, TaskDto[].class));

            List<String> violationMessages = taskDtos
                    .stream()
                    .flatMap(dto -> validator.validate(dto).stream())
                    .map(ConstraintViolation::getMessage)
                    .toList();

            if (!violationMessages.isEmpty()) {
                return ResponseEntity.badRequest().body(String.join("\n", violationMessages));
            }

            // Call service
            taskService.createMultipleTasks(taskDtos);

            return ResponseEntity.ok("Uploaded " + taskDtos.size() + " tasks");
        }
        catch (Exception e) {
            throw new InvalidFileFormatException("Invalid file format");
        }
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
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
