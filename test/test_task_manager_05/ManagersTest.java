package test_task_manager_05;

import org.junit.jupiter.api.Test;
import taskmanager.controllers.*;
import taskmanager.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    public void testTaskManager() {
        TaskManager manager = new InMemoryTaskManager();

        assertNotNull(manager, "Задачи не найдена.");
        assertEquals(InMemoryTaskManager.class, manager.getClass());
    }

    @Test
    public void testAddTaskHistoryManager() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();

        Task task = new Task();
        manager.add(task);
        final List<Task> taskHistory = manager.getHistory();

        assertEquals(1, taskHistory.size(), "После добавления количество задач должно соотвествовать.");
        assertEquals(task, taskHistory.get(0), "После добавления задачи она должна быть в истории");
    }
    @Test
    public void testCheckOrderGetTaskHistoryManager() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();

        Task task1 = new Task();
        task1.setId(1);
        manager.add(task1);
        Task task2 = new Task();
        task2.setId(2);
        manager.add(task2);
        manager.add(task1);
        final List<Task> taskHistory = manager.getHistory();

        assertEquals(task2, taskHistory.get(0)); // порядок расположения задач в списке сохраняется
        assertEquals(task1, taskHistory.get(1));
    }
    @Test
    public void testRemoveHistoryManager() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();

        Task task = new Task();
        task.setId(1);
        manager.add(task);
        manager.remove(task.getId());
        final List<Task> history = manager.getHistory();

        assertTrue(history.isEmpty(), "После удаления задачи по id задачи в HashMap быть не должно.");
    }
}

