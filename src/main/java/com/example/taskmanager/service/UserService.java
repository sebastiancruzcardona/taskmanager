package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserDto getUser(Integer id) {

        log.info("Getting user with id {}", id);

        return userRepository.findById(id)
                .map(user -> {
                    log.debug("User with id {} found", user.getId());
                    return modelMapper.map(user, UserDto.class);
                })
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserNotFoundException(id);
                });
    }

    public UserWithTaskDto getUserWithTasks(Integer id) {

        log.info("Getting user with tasks with id {}", id);

        return userRepository.findById(id)
                .map(user -> {
                    log.debug("User with id {} found", user.getId());
                    return modelMapper.map(user, UserWithTaskDto.class);
                })
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserNotFoundException(id);
                });
    }
}
