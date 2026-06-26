package com.example.taskmanager.dto;

import com.example.taskmanager.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotBlank;

public class TaskDto {

    private Integer id;
    private String title;
    private String description;

    private TaskStatusEnum status;

    // Default Constructor
    public TaskDto() {
    }

    // All parameter Constructor
    public TaskDto(Integer id, String title, String description, TaskStatusEnum status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatusEnum getStatus() {
        return status;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatusEnum status) {
        this.status = status;
    }
}
