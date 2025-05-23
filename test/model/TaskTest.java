package model;

import org.junit.jupiter.api.Test;
import taskmanager.controller.HistoryManager;
import taskmanager.controller.Managers;
import taskmanager.model.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static taskmanager.util.Status.NEW;

class TaskTest {
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    public void testTaskEqualsTask() {

        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW);
        task.setId(1);
        //inMemoryTaskManager.addTask(task);
        historyManager.add(task);

        Task savedTask = null;

        for(Task t : historyManager.getHistory()) {
            if (t.getId() == task.getId()) {
                savedTask = t;
                break;
            }
        }

        assertEquals(task, savedTask, "Задачи не совпадают.");
    }
}