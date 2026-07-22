package com.example.taskmanager.dto;

import com.example.taskmanager.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskWithUserDto {

    private Integer id;

    @NotBlank(message = "A title must be provided")
    private String title;

    @NotBlank(message = "A description must be provided")
    private String description;

    private TaskStatusEnum status;

    private UserDto user;
}
