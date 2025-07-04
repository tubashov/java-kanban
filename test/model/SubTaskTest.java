package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import taskmanager.controller.InMemoryTaskManager;
import taskmanager.model.Epic;
import taskmanager.model.SubTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static taskmanager.util.Status.NEW;

public class SubTaskTest {

    static InMemoryTaskManager inMemoryTaskManager;

    @BeforeAll
    static void beforeAll() {
        inMemoryTaskManager = new InMemoryTaskManager();
    }

    @Test
    public void testSubTaskEqualsSubTask() {

        SubTask subTask = new SubTask(1,"Подзадача", "Описание", NEW,
                LocalDateTime.of(2025, 6, 20, 18, 00),
                Duration.ofMinutes(10));

        inMemoryTaskManager.addSubTask(subTask);

        final int subTaskId = subTask.getId();

        final SubTask savedSubTask = inMemoryTaskManager.getSubTask(subTaskId);

        assertEquals(subTask, savedSubTask, "Задачи не совпадают.");
    }

    @Test
    public void testSubTaskNotEqualsEpic() {

        SubTask subTask = new SubTask(1,"Подзадача", "Описание", NEW,
                LocalDateTime.of(2025, 6, 20, 18, 30),
                Duration.ofMinutes(10));
        inMemoryTaskManager.addSubTask(subTask);

        Epic epic = new Epic(1, "Test addEpic name", "Test addEpic description", NEW);
        inMemoryTaskManager.addEpic(epic);

        ArrayList<SubTask> subTasks = inMemoryTaskManager.getSubTasks();
        ArrayList<Epic> epics = inMemoryTaskManager.getEpics();

        assertNotEquals(subTasks.get(0), epics.get(0), "Задачи совпадают.");

    }
}