package com.example.taskmanager.service;

import com.example.taskmanager.enums.TaskStatusEnum;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskStatusRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
public class TaskStatusServiceTest {

    @MockitoBean
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusService taskStatusService;

    @AfterEach
    void tearDown() {
        taskStatusService.clearTaskStatusCache();
    }

    @Test
    void getByCode_taskStatusFound() {
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.PENDING;

        TaskStatus expectedTaskStatus = new TaskStatus();
        expectedTaskStatus.setCode(taskStatusEnum);
        expectedTaskStatus.setDescription("Pending");

        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.of(expectedTaskStatus));

        TaskStatus result = taskStatusService.getByCode(taskStatusEnum);

        assertNotNull(result);
        assertEquals(taskStatusEnum, result.getCode());
        assertEquals("Pending", result.getDescription());

    }

    @Test
    void getByCode_taskStatusNotFound() {
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.COMPLETED;

        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskStatusService.getByCode(taskStatusEnum)
        );

        assertEquals("Status not found", exception.getMessage());
    }

    @Test
    void testCacheBehavior() {
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.PENDING;

        TaskStatus expectedTaskStatus = new TaskStatus();
        expectedTaskStatus.setCode(taskStatusEnum);
        expectedTaskStatus.setDescription("Pending");

        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.of(expectedTaskStatus));

        TaskStatus result1 = taskStatusService.getByCode(taskStatusEnum);

        assertNotNull(result1);
        assertEquals("Pending", result1.getDescription());

        TaskStatus result2 = taskStatusService.getByCode(taskStatusEnum);

        assertSame(result1, result2);

        verify(taskStatusRepository, times(1))
                .findTaskStatusByCode(taskStatusEnum);
    }

    @Test
    void testCacheIsClearedAfterEviction() {
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.PENDING;

        TaskStatus firstStatus = new TaskStatus();
        firstStatus.setCode(taskStatusEnum);
        firstStatus.setDescription("Pending");

        TaskStatus secondStatus = new TaskStatus();
        secondStatus.setCode(taskStatusEnum);
        secondStatus.setDescription("Pending updated");

        // First DB call
        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.of(firstStatus));

        // Load into cache
        TaskStatus result1 = taskStatusService.getByCode(taskStatusEnum);
        assertEquals("Pending", result1.getDescription());

        // Should be cached
        TaskStatus cachedResult = taskStatusService.getByCode(taskStatusEnum);
        assertSame(result1, cachedResult);

        // Evict cache (simulates 6h passing)
        taskStatusService.clearTaskStatusCache();

        // After eviction, repository should be called again
        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.of(secondStatus));

        // Second DB cal
        TaskStatus resultAfterEviction = taskStatusService.getByCode(taskStatusEnum);
        assertEquals("Pending updated", resultAfterEviction.getDescription());

        // Repository called twice in total
        verify(taskStatusRepository, times(2))
                .findTaskStatusByCode(taskStatusEnum);

    }
}
