package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskWithUserDto;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.parser.TasksFileParser;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final TasksFileParser parser;

    public TaskDto createTask(TaskDto taskDto) {

        log.info("Creating task with title: {}", taskDto.getTitle());

        Task task = modelMapper.map(taskDto, Task.class);
        Task savedTask = taskRepository.save(task);

        log.info("Task created successfully with id: {}", savedTask.getId());

        return modelMapper.map(savedTask, TaskDto.class);
    }

    public int createMultipleTasks(MultipartFile file) {

        List<TaskDto> taskDtos = parser.parseTaskDtos(file);

        log.info("Creating {} tasks in bulk", taskDtos.size());

        List<Task> tasks = taskDtos
                .stream()
                .map(taskDto -> modelMapper.map(taskDto, Task.class))
                .toList();

        taskRepository.saveAll(tasks);

        log.info("Bulk task creation completed successfully");

        return tasks.size();
    }

    public Page<TaskDto> getAllTasks(Pageable pageable) {

        log.info("Fetching tasks with page: {}, size: {} sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );

        Page<Task> tasksPage = taskRepository.findAll(pageable);

        log.info("Fetched {} tasks from database", tasksPage.getNumberOfElements());

        return tasksPage
                .map(task -> modelMapper.map(task, TaskDto.class));
    }

    public TaskWithUserDto getTaskById(Integer id) {

        log.info("Fetching task with user with id: {}", id);

        return taskRepository.findById(id)
                .map(task -> {
                    log.debug("Task found. Id: {}", task.getId());
                    return modelMapper.map(task, TaskWithUserDto.class);
                })
                .orElseThrow(() -> {
                    log.error("Task with id {} not found", id);
                    return new TaskNotFoundException(id);
                });
    }

    public TaskDto updateTask(Integer id, TaskDto taskDto) {

        log.info("Updating task with id: {}", id);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update the task. Task with id {} not found", id);
                    return new TaskNotFoundException(id);
                });

        log.debug("Task found with id: {}", existingTask.getId());

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());

        Task updatedTask = taskRepository.save(existingTask);

        log.info("Task with id {} updated successfully", updatedTask.getId());

        return modelMapper.map(updatedTask, TaskDto.class);
    }

    public void deleteTask(Integer id) {

        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            log.error("Cannot delete. Task with id {} not found", id);
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);

        log.info("Task with id {} deleted successfully", id);
    }
}
