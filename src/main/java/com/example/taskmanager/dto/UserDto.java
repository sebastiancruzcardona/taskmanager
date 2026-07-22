package com.example.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Integer id;

    @NotBlank(message = "A name must be provided")
    private String name;

    @NotBlank(message = "An email must be provided")
    @Email(message = "You must provide a valid email")
    private String email;
}
