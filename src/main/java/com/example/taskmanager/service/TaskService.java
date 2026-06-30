package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.exception.FieldConstraintsViolationException;
import com.example.taskmanager.exception.InvalidFileFormatException;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public TaskDto createTask(TaskDto taskDto) {
        Task task = modelMapper.map(taskDto, Task.class);
        Task savedTask = taskRepository.save(task);

        return modelMapper.map(savedTask, TaskDto.class);
    }

    public int createMultipleTasks(MultipartFile file) {
        List<Task> tasks = transformToListOfTaskDtos(file)
                .stream()
                .map(taskDto -> modelMapper.map(taskDto, Task.class))
                .toList();

        taskRepository.saveAll(tasks);

        return tasks.size();
    }

    public List<TaskDto> transformToListOfTaskDtos(MultipartFile file) {
        try {
            String json = new String(file.getBytes());

            List<TaskDto> taskDtos = Arrays.asList(objectMapper.readValue(json, TaskDto[].class));

            List<String> violationMessages = taskDtos.stream()
                    .flatMap(dto -> validator.validate(dto).stream())
                    .map(violation ->
                            violation.getPropertyPath() + ": " + violation.getMessage())
                    .toList();

            if (!violationMessages.isEmpty()) {
                throw new FieldConstraintsViolationException(String.join("\n", violationMessages));
            }

            return taskDtos;

        }
        catch (Exception e) {
            throw new InvalidFileFormatException("Invalid file format");
        }
    }

    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(task -> modelMapper.map(task, TaskDto.class))
                .toList();
    }

    public TaskDto getTaskById(Integer id) {
        return taskRepository.findById(id)
                .map(task -> modelMapper.map(task, TaskDto.class))
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public TaskDto updateTask(Integer id, TaskDto taskDto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());

        Task updatedTask = taskRepository.save(existingTask);

        return modelMapper.map(updatedTask, TaskDto.class);
    }

    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}
