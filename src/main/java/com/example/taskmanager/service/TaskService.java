package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskWithUserDto;
import com.example.taskmanager.enums.RoleEnum;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.parser.TasksFileParser;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final TaskStatusService taskStatusService;

    public TaskDto createTask(TaskDto taskDto) {

        log.info("Creating task with title: {}", taskDto.getTitle());

        Task task = modelMapper.map(taskDto, Task.class);
        task.setStatus(taskStatusService.getByCode(taskDto.getStatus()));

        Task savedTask = taskRepository.save(task);

        log.info("Task created successfully with id: {}", savedTask.getId());

        TaskDto adjustedTaskDto = modelMapper.map(savedTask, TaskDto.class);
        adjustedTaskDto.setStatus(savedTask.getStatus().getCode());

        return adjustedTaskDto;
    }

    public int createMultipleTasks(MultipartFile file) {

        List<TaskDto> taskDtos = parser.parseTaskDtos(file);

        log.info("Creating {} tasks in bulk", taskDtos.size());

        List<Task> tasks = taskDtos
                .stream()
                .map(taskDto -> {
                    Task task = modelMapper.map(taskDto, Task.class);
                    task.setStatus(taskStatusService.getByCode(taskDto.getStatus()));
                    return task;
                })
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
                .map(task -> {
                    TaskDto taskDto = modelMapper.map(task, TaskDto.class);
                    taskDto.setStatus(task.getStatus().getCode());

                    return taskDto;
                });
    }

    public TaskWithUserDto getTaskById(AuthenticatedUser currentUser, Integer id) {

        log.info("Fetching task with user with id: {}", id);

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        // Fetch the task or throw exception if not found
        Task task = taskRepository.findById(id) // better pattern would be findByIdAndEmail(id, email)
                .orElseThrow(() -> {
                    log.error("Task with id {} not found", id);
                    return new TaskNotFoundException(id);
                });

        // Enforce ownership: only the owner or ADMIN can access
        if (currentUser.getRole() != RoleEnum.ADMIN && task.getUser() != null &&
                !task.getUser().getEmail().equals(currentUser.getEmail())) { // better with getId, because id never changes
            log.error("User {} is not allowed to access task with id {}", currentUser.getEmail(), id);
            throw new AccessDeniedException("You do not own this task");
        }

        // Map entity to DTO
        TaskWithUserDto taskDto = modelMapper.map(task, TaskWithUserDto.class);
        taskDto.setStatus(task.getStatus().getCode());

        log.debug("Returning task DTO for task id {}", task.getId());
        return taskDto;
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
        existingTask.setStatus(taskStatusService.getByCode(taskDto.getStatus()));

        Task updatedTask = taskRepository.save(existingTask);

        log.info("Task with id {} updated successfully", updatedTask.getId());

        TaskDto returnedTaskDto = modelMapper.map(updatedTask, TaskDto.class);
        returnedTaskDto.setStatus(updatedTask.getStatus().getCode());

        return returnedTaskDto;
    }

    /*public void updateTasks() {
        List<Task> tasks = taskRepository.findAll();

        tasks.forEach(task -> {
            task.setTask_status_id(taskStatusService.getByCode(task.getStatus()));
        });

        taskRepository.saveAll(tasks);
    }*/

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
