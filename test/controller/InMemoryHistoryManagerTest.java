package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.controller.HistoryManager;
import taskmanager.controller.InMemoryHistoryManager;
import taskmanager.model.Task;
import taskmanager.util.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    HistoryManager historyManager;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 0),
                Duration.ofMinutes(10));

        task2 = new Task(2, "Задача 2", "Описание 2", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 20),
                Duration.ofMinutes(20));

        task3 = new Task(3, "Задача 3", "Описание 3", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 40),
                Duration.ofMinutes(30));
        historyManager = new InMemoryHistoryManager();
    }

    // пустая история
    @Test
    void shouldReturnEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    // проверка дублирования
    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.getFirst());
    }

    @Test
    void shouldNotDuplicateTask() {
        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Задача не должна дублироваться");
    }

    // удаление из начала истории
    @Test
    void shouldRemoveTaskFromBeginnimg() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertFalse(history.contains(task1));
    }

    // удаление задачи из середины истории
    @Test
    void shouldRemoveTaskFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertFalse(history.contains(task2));
    }

    // удаление из конца истории
    @Test
    void shouldRemoveTaskFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertFalse(history.contains(task1));
    }

    // сохранение задач по порядку
    @Test
    void saveOrderOfTask() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task1,task2,task3), history, "Порядок задач в истории должен сохраняться");
    }

    // изменение порядка при повторном просмотре
    @Test
    void shouldMoveTaskToEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task1,task3,task2), history, "Повторно просмотренная задача должна переместиться" +
                "в конец истории");
    }
}
