package com.example.taskmanager.model;

import com.example.taskmanager.enums.TaskStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING) // This is an enum and the db wants it as a String
    private TaskStatusEnum status;

    // insertable = false -> tells hibernate don't include this in the insert to force default now() in db
    // updatable = false -> tells hibernate that it won't try to update this later
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
