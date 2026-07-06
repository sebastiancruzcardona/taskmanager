package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // TODO: Create the proper exception and replace RuntimeException

    public UserDto getUser(Integer id) {
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserDto.class))
                .orElseThrow(() -> new RuntimeException("User with id " + id + "not found"));
    }

    public UserWithTaskDto getUserWithTasks(Integer id) {
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserWithTaskDto.class))
                .orElseThrow(() -> new RuntimeException("User with id " + id + "not found"));
    }
}
