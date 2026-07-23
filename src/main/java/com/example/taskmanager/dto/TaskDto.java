package com.example.taskmanager.dto;

import com.example.taskmanager.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Integer id;

    @NotBlank(message = "A title must be provided")
    private String title;

    private String description;

    private TaskStatusEnum status;
}
