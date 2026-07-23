package com.example.taskmanager.integration;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        User savedUser = new User();
        savedUser.setName("Sebastian");
        savedUser.setEmail("sbs@example.com");
        userRepository.save(savedUser);
    }

    // ---------------------------
    // getUserById
    // ---------------------------

    @Test
    void getUserById_shouldReturn200_whenUserExists() throws Exception {
        mockMvc.perform(get("/users/withTasks/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("sbs@example.com"));
    }

    @Test
    void getUserById_shouldReturn404_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/withTasks/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
