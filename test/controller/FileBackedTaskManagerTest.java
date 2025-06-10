package controller;
import org.junit.jupiter.api.*;
import taskmanager.model.*;
import taskmanager.controller.FileBackedTaskManager;
import taskmanager.util.Status;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    // создание временного файла для тестирования
    @BeforeEach
    void setup() {
        try {
            tempFile = File.createTempFile("test_tasks", ".csv");
            manager = new FileBackedTaskManager(tempFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }

    // удаление файла после каждого теста
    @AfterEach
    void cleanup() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // загрузка пустого файла
    @Test
    void testSaveAndLoadEmptyFile() {
        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(restored.getTasks().isEmpty());
        assertTrue(restored.getEpics().isEmpty());
        assertTrue(restored.getSubTasks().isEmpty());
    }

    // сохранение и загрузка задачи
    @Test
    void testSaveAndLoadSimpleTasks() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        manager.addTask(task);

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasks = restored.getTasks();
        assertEquals(1, tasks.size());
        assertEquals(task.getName(), tasks.get(0).getName());
        assertEquals(task.getDescription(), tasks.get(0).getDescription());
    }

    // сохранение и загрузка эпика с подзадачей
    @Test
    void testSaveAndLoadEpicWithSubTasks() {
        Epic epic = new Epic("Epic1", "Epic Description", Status.NEW);
        manager.addEpic(epic);

        SubTask subTask1 = new SubTask("Sub Task 1", "Description 1", Status.NEW, epic.getId());
        SubTask subTask2 = new SubTask("Sub Task 2", "Description 2", Status.DOWN, epic.getId());
        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(tempFile);

        ArrayList<Epic> epics = restored.getEpics();
        ArrayList<SubTask> subs = restored.getSubTasks();

        assertEquals(1, epics.size());
        assertEquals(2, subs.size());

        SubTask restoredSubTask1 = subs.get(0);
        assertEquals(epic.getId(), restoredSubTask1.getEpicId());
    }

    // сохранение истории
    @Test
    void testHistorySaved() {
        Task task = new Task("History Task", "Track me", Status.NEW);
        manager.addTask(task);
        manager.getTask(task.getId());

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> history = restored.getHistory();
        assertEquals(1, history.size());
    }
}
