package com.example.taskmanager.security;

import com.example.taskmanager.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticatedUser {

    private final String email;
    private final RoleEnum role;
}
