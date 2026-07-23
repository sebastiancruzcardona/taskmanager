package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    // ---------------------------
    // getUserById
    // ---------------------------

    @Test
    void getUserById_shouldReturnUserDto_whenUserExists() {
        Integer userId = 1;

        User user = new User();
        user.setId(userId);

        UserWithTaskDto userWithTaskDto = new UserWithTaskDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(modelMapper.map(user, UserWithTaskDto.class))
                .thenReturn(userWithTaskDto);

        UserWithTaskDto result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userWithTaskDto);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {
        Integer userId = 1;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id " + userId + " not found");
    }
}
