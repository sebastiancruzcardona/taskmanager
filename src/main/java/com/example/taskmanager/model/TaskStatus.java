package com.example.taskmanager.model;

import com.example.taskmanager.enums.TaskStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_statuses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatusEnum code;

    @Column(name = "description", nullable = false)
    private String description;
}
