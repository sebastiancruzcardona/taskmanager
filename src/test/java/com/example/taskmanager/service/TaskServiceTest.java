package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskWithUserDto;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.parser.TasksFileParser;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TasksFileParser parser;

    @InjectMocks
    private TaskService taskService;

    // ---------------------------
    // createTask
    // ---------------------------

    @Test
    void createTask_shouldSaveAndReturnTaskDto() {
        TaskDto inputDto = new TaskDto();
        inputDto.setTitle("Learn Spring");

        Task taskEntity = new Task();

        Task savedTask = new Task();
        savedTask.setId(1);

        TaskDto returnedDto = new TaskDto();
        returnedDto.setTitle("Learn Spring");

        when(modelMapper.map(inputDto, Task.class))
                .thenReturn(taskEntity);

        when(taskRepository.save(taskEntity))
                .thenReturn(savedTask);

        when(modelMapper.map(savedTask, TaskDto.class))
                .thenReturn(returnedDto);

        TaskDto result = taskService.createTask(inputDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Learn Spring");
    }

    // ---------------------------
    // createMultipleTasks
    // ---------------------------

    @Test
    void createMultipleTasks_shouldSaveAllTasksAndReturnSize() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        TaskDto dto1 = new TaskDto();
        TaskDto dto2 = new TaskDto();

        Task task1 = new Task();
        Task task2 = new Task();

        when(parser.parseTaskDtos(multipartFile))
                .thenReturn(List.of(dto1, dto2));

        when(modelMapper.map(dto1, Task.class))
                .thenReturn(task1);

        when(modelMapper.map(dto2, Task.class))
                .thenReturn(task2);

        int result = taskService.createMultipleTasks(multipartFile);

        assertThat(result).isEqualTo(2);

        verify(taskRepository).saveAll(List.of(task1, task2));
        verify(taskRepository, times(1)).saveAll(any());
    }

    // ---------------------------
    // getAllTasks
    // ---------------------------

    @Test
    void getAllTasks_shouldReturnMappedPage() {
        Sort sort = Sort.by("title").ascending();
        Pageable pageable = PageRequest.of(0, 5, sort);

        Task task1 = new Task();
        Task task2 = new Task();

        TaskDto dto1 = new TaskDto();
        TaskDto dto2 = new TaskDto();

        when(taskRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(task1, task2), pageable, 2));

        when(modelMapper.map(task1, TaskDto.class))
                .thenReturn(dto1);

        when(modelMapper.map(task2, TaskDto.class))
                .thenReturn(dto2);

        Page<TaskDto> result = taskService.getAllTasks(pageable);

        assertThat(2).isEqualTo(result.getTotalElements());

        verify(taskRepository, times(1)).findAll(pageable);
    }

    // ---------------------------
    // getTaskById
    // ---------------------------

    @Test
    void getTaskById_shouldReturnTaskWithDto() {
        Integer id = 1;

        Task task = new Task();
        task.setId(id);

        TaskWithUserDto dto = new TaskWithUserDto();

        when(taskRepository.findById(id))
                .thenReturn(Optional.of(task));

        when(modelMapper.map(task, TaskWithUserDto.class))
                .thenReturn(dto);

        TaskWithUserDto result = taskService.getTaskById(id);

        assertThat(result).isNotNull();
    }

    @Test
    void getTaskById_shouldThrowException_whenTaskNotFound() {
        Integer id = 1;

        when(taskRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id " + id + " not found");
    }

    // ---------------------------
    // updateTask
    // ---------------------------

    @Test
    void updateTask_shouldUpdateAndReturnTaskDto() {
        Integer id = 1;

        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated");

        Task existingTask = new Task();
        existingTask.setId(id);

        Task updatedTask = new Task();
        updatedTask.setId(id);

        TaskDto returnedDto = new TaskDto();
        returnedDto.setTitle("Updated");

        when(taskRepository.findById(id))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(existingTask))
                .thenReturn(updatedTask);

        when(modelMapper.map(updatedTask, TaskDto.class))
                .thenReturn(returnedDto);

        TaskDto result = taskService.updateTask(id, updateDto);

        assertThat(result.getTitle()).isEqualTo("Updated");
    }

    @Test
    void updateTask_shouldThrowException_whenTaskNotFound() {
        Integer id = 1;

        TaskDto updateDto = new TaskDto();

        when(taskRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(id, updateDto))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id " + id + " not found");
    }

    // ---------------------------
    // deleteTask
    // ---------------------------

    @Test
    void deleteTask_shouldReturnTaskAndDeleteIt() {
        Integer id = 1;

        when(taskRepository.existsById(id))
                .thenReturn(true);

        doNothing().when(taskRepository).deleteById(id);

        taskService.deleteTask(id);

        verify(taskRepository, times(1))
                .deleteById(id);
    }
}
