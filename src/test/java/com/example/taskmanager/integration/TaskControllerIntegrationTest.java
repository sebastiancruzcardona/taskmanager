package com.example.taskmanager.integration;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskWithUserDto;
import com.example.taskmanager.enums.TaskStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TaskControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ---------------------------
    // POST /tasks
    // ---------------------------

    @Test
    void createTask_shouldCreateTask() throws Exception {
        createTask(TaskDto.builder()
                .title("New Task")
                .description("Integration test")
                .status(TaskStatusEnum.NEW)
                .build()
        );
    }

    @Test
    void createTask_shouldFailOnValidation() throws Exception {
        TaskDto dto = new TaskDto(); // invalid

        mockMvc.perform(post("/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("title=A title must be provided")));
    }

    // ---------------------------
    // POST /tasks/upload
    // ---------------------------

    @Test
    void uploadTasks_shouldUploadValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tasks.json",
                MediaType.APPLICATION_JSON_VALUE,
                """
                [
                { "title": "Bulk 1", "description": "test", "status": "NEW" },
                { "title": "Bulk 2", "description": "test", "status": "NEW" }
                ]
                """.getBytes());

        mockMvc.perform(multipart("/tasks/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Uploaded 2 tasks")));
    }

    @Test
    void uploadTasks_shouldFailOnValidationErrors() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tasks.json",
                MediaType.APPLICATION_JSON_VALUE,
                """
                [
                { "title": ""}
                ]
                """.getBytes()
        );

        mockMvc.perform(multipart("/tasks/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("title: A title must be provided")));
    }

    @Test
    void uploadTasks_shouldFailOnInvalidFileFormat() throws  Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tasks.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not json".getBytes()
        );

        mockMvc.perform(multipart("/tasks/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid file format")));
    }

    // ---------------------------
    // GET /tasks
    // ---------------------------

    @Test
    void getAllTasks_shouldReturnPagedResult() throws Exception {
        mockMvc.perform(get("/tasks")
                    .param("page", "0")
                    .param("size", "5")
                    .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5));
    }

    // ---------------------------
    // GET /tasks/{id}
    // ---------------------------

    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        String response = createTask(TaskDto.builder()
                .title("Find me")
                .description("Find me")
                .status(TaskStatusEnum.NEW)
                .build()
        );

        TaskWithUserDto created = objectMapper.readValue(response, TaskWithUserDto.class);

        mockMvc.perform(get("/tasks/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Find me"));
    }

    @Test
    void getTaskById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 999))
                .andExpect(status().isNotFound());
    }

    // ---------------------------
    // PUT /tasks/{id}
    // ---------------------------

    @Test
    void updateTask_shouldUpdateTask() throws Exception {
        String response = createTask(TaskDto.builder()
                .title("Original")
                .description("Before update")
                .status(TaskStatusEnum.NEW)
                .build()
        );

        TaskDto created = objectMapper.readValue(response, TaskDto.class);

        mockMvc.perform(put("/tasks/{id}", created.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                             {
                               "title": "Updated",
                               "description": "After update",
                               "status": "PENDING"
                             }
                             """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void updateTask_shouldFailOnValidation() throws Exception {
        mockMvc.perform(put("/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                           "title": "",
                           "description": "After update",
                           "status": "PENDING"}
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("title=")));
    }

    // ---------------------------
    // DELETE /tasks/{id}
    // ---------------------------

    @Test
    void deleteTask_shouldDeleteSuccessfully() throws Exception {
        String response = createTask(TaskDto.builder()
                .title("Delete me")
                .description("To delete")
                .status(TaskStatusEnum.COMPLETED)
                .build()
        );

        TaskDto created = objectMapper.readValue(response, TaskDto.class);

        mockMvc.perform(delete("/tasks/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    private String createTask(TaskDto dto) throws Exception {
        return mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.status").value(dto.getStatus().name()))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
