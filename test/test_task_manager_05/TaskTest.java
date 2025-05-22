package test_task_manager_05;

import org.junit.jupiter.api.Test;
import taskmanager.controllers.InMemoryTaskManager;
import taskmanager.model.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static taskmanager.util.Status.NEW;

class TaskTest {

    @Test
    public void testTaskEqualsTask() {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW);
        task.setId(1);
        inMemoryTaskManager.addTask(task);

        final int taskId = task.getId();

        final Task savedTask = inMemoryTaskManager.getTask(taskId);

        assertEquals(task, savedTask, "Задачи не совпадают.");
    }
}