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

    class EpicTest {
        static InMemoryTaskManager inMemoryTaskManager;

        @BeforeAll
        static void beforeAll() {
            inMemoryTaskManager = new InMemoryTaskManager();
        }

        @Test
        public void testEpicEqualsEpic() {
            Epic epic = new Epic(1, "Test addEpic name", "Test addEpic description", NEW);

            inMemoryTaskManager.addEpic(epic); // добавить эпик в менеджер

            final int epicId = epic.getId();
            final Epic savedEpic = inMemoryTaskManager.getEpic(epicId);

            assertEquals(epic, savedEpic, "Задачи не совпадают.");
        }

        @Test
        public void testEpicNotEqualsSubTask() {

            LocalDateTime now = LocalDateTime.of(2025, 6, 15, 10, 0);

            Epic epic = new Epic(1, "Эпик 1", "Тест-эпик 1", NEW);
            inMemoryTaskManager.addEpic(epic);

            SubTask subTask = new SubTask(1, "Подзадача 1", "Тест-подзадча 1", now.plusHours(1),
                    Duration.ofMinutes(5), NEW);
            inMemoryTaskManager.addSubTask(subTask);//

            ArrayList<Epic> epics = inMemoryTaskManager.getEpics();
            ArrayList<SubTask> subTasks = inMemoryTaskManager.getSubTasks();

            assertNotEquals(epics.get(0), subTasks.get(0), "Задачи совпадают.");

        }
    }