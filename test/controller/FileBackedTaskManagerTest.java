package controller;
import org.junit.jupiter.api.*;
import taskmanager.model.*;
import taskmanager.controller.FileBackedTaskManager;
import taskmanager.util.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

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
        Task task = new Task(1, "Задача 1", "Тест-задача 1", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 0),
                Duration.ofMinutes(10));
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
        Epic epic = new Epic(1, "Эпик 1", "Тест-эпик 1", Status.NEW);
        manager.addEpic(epic);

        SubTask subTask1 = new SubTask(1, "Подзадача 1", "Тест-подзадача 1",
                Status.NEW, LocalDateTime.of(2025, 6, 20, 18, 0),
                Duration.ofMinutes(15), epic.getId());
        SubTask subTask2 = new SubTask(2, "Подзадача 2", "Тест-подзадача 2",
                Status.NEW, LocalDateTime.of(2025, 6, 20, 18, 20),
                Duration.ofMinutes(15), epic.getId());
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
        Task task = new  Task(1, "Задача 1", "Тест-задача 1", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 0),
                Duration.ofMinutes(10));
        manager.addTask(task);
        manager.getTask(task.getId());

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> history = restored.getHistory();
        assertEquals(1, history.size());
    }

    // исключение при отсутствии файла
    @Test
    void shouldThrowIOExceptionWhenFileDoesNotExist() {
        File invalidFile = new File("non_existing_file.csv");

        assertThrows(RuntimeException.class, () -> {
            FileBackedTaskManager.loadFromFile(invalidFile);
        }, "Должно выбрасываться исключение, если файл не существует");
    }

    // чтение корректного файла
    @Test
    void shouldNotThrowWhenReadingValidFile() throws IOException {
        File validFile = new File("valid_tasks.csv");

        try (FileWriter writer = new FileWriter(validFile)) {
            writer.write("id,type,name,status,description\n"); // Заголовок CSV
        }

        assertDoesNotThrow(() -> {
            FileBackedTaskManager.loadFromFile(validFile);
        }, "Чтение корректного файла не должно выбрасывать исключений");

        validFile.delete(); // удалить файл после теста
    }
}
