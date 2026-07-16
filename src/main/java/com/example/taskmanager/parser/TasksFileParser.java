package com.example.taskmanager.parser;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.exception.FieldConstraintsViolationException;
import com.example.taskmanager.exception.InvalidFileFormatException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Validator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TasksFileParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public List<TaskDto> parseTaskDtos(MultipartFile file) {

        log.info("POST /tasks/upload called with file {}", file.getOriginalFilename());

        try {
            String json = new String(file.getBytes());

            List<TaskDto> taskDtos = Arrays.asList(objectMapper.readValue(json, TaskDto[].class));

            List<String> violationMessages = taskDtos.stream()
                    .flatMap(dto -> validator.validate(dto).stream())
                    .map(violation ->
                            violation.getPropertyPath() + ": " + violation.getMessage())
                    .toList();

            if (!violationMessages.isEmpty()) {
                log.warn("Validation failed while uploading files with {} violations", violationMessages.size());
                throw new FieldConstraintsViolationException(String.join("\n", violationMessages));
            }

            log.info("File uploaded successfully");

            return taskDtos;

        }
        catch (JsonProcessingException e) {
            log.error("Failed to parse file uploaded", e);
            throw new InvalidFileFormatException("Invalid file format");
        }
        catch (IOException e) {
            log.error("Failed to read file", e);
            throw new InvalidFileFormatException("Invalid file format");
        }
    }
}
