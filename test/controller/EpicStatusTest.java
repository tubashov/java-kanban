package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.controller.InMemoryTaskManager;
import taskmanager.model.*;
import taskmanager.util.Status;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicStatusTest {

    private InMemoryTaskManager manager;
    private Epic epic;
    private int epicId;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic("Эпик", "Описание эпика", Status.NEW);
        manager.addEpic(epic);
        epicId = epic.getId();
    }

    private void addSubTask(Status status) {
        SubTask sub = new SubTask(1,"Подзадача", "Описание", status,
                LocalDateTime.now(), Duration.ofMinutes(10), epicId);
        manager.addSubTask(sub);
    }

    @Test
    void shouldReturnNEW_WhenAllSubtasksAreNew() {
        addSubTask(Status.NEW);
        addSubTask(Status.NEW);

        assertEquals(Status.NEW, manager.updateEpicStatus(epicId));
    }

    @Test
    void shouldReturnDONE_WhenAllSubtasksAreDone() {
        addSubTask(Status.DONE);
        addSubTask(Status.DONE);

        assertEquals(Status.DONE, manager.updateEpicStatus(epicId));
    }

    @Test
    void shouldReturnIN_PROGRESS_WhenMixedNewAndDone() {
        addSubTask(Status.NEW);
        addSubTask(Status.DONE);

        assertEquals(Status.IN_PROGRESS, manager.updateEpicStatus(epicId));
    }

    @Test
    void shouldReturnIN_PROGRESS_WhenAllSubtasksInProgress() {
        addSubTask(Status.IN_PROGRESS);
        addSubTask(Status.IN_PROGRESS);

        assertEquals(Status.IN_PROGRESS, manager.updateEpicStatus(epicId));
    }

    @Test
    void shouldReturnNEW_WhenNoSubtasks() {
        assertEquals(Status.NEW, manager.updateEpicStatus(epicId));
    }
}