package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.controller.TaskManager;
import taskmanager.model.*;
import taskmanager.util.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }


    @Test
    void shouldGenerateUniqueIds() {
        int id1 = manager.generateId();
        int id2 = manager.generateId();
        assertNotEquals(id1, id2, "ID должны быть уникальными");
        assertTrue(id2 > id1, "ID должен увеличиваться");
    }

    @Test
    void shouldAddTaskSuccessfully() {
        Task task = new Task(1, "Тестовая задача", "Описание", Status.NEW,
                LocalDateTime.of(2024, 6, 15, 10, 0),
                Duration.ofMinutes(60));

        Task addedTask = manager.addTask(task);

        assertNotNull(addedTask.getId(), "ID должен быть присвоен задаче");
        Task retrievedTask = manager.getTask(addedTask.getId());
        assertEquals(task.getName(), retrievedTask.getName(), "Имя задачи должно совпадать");
        assertEquals(task.getDescription(), retrievedTask.getDescription(), "Описание должно совпадать");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус должен совпадать");
    }
    @Test
    void shouldAddEpicAndGetItById() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        Epic addedEpic = manager.addEpic(epic);

        assertNotNull(addedEpic.getId());
        Epic retrieved = manager.getEpic(addedEpic.getId());

        assertEquals(epic.getName(), retrieved.getName());
        assertEquals(epic.getDescription(), retrieved.getDescription());
    }

    @Test
    void shouldAddSubTaskAndGetItById() {
        Epic epic = manager.addEpic(new Epic("Эпик", "Описание"));
        SubTask sub = new SubTask("Подзадача", "Описание", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30), epic.getId());
        SubTask addedSub = manager.addSubTask(sub);

        assertNotNull(addedSub.getId());
        SubTask retrieved = manager.getSubTask(addedSub.getId());
        assertEquals(sub.getName(), retrieved.getName());
        assertEquals(epic.getId(), retrieved.getEpicId());
    }

    @Test
    void shouldReturnAllTasksAndSubTasksAndEpics() {
        manager.addTask(new Task("Задача", "Описание", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addEpic(new Epic("Эпик", "Описание эпика"));
        manager.addSubTask(new SubTask("Подзадача", "Описание", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15), 2));

        assertEquals(1, manager.getTasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(1, manager.getSubTasks().size());
    }

    @Test
    void shouldReturnEmptyListIfNoSubTasksInEpic() {
        Epic epic = manager.addEpic(new Epic("Эпик без подзадач", "Описание"));
        assertTrue(manager.getSubTaskList(epic.getId()).isEmpty());
    }

    @Test
    void shouldReturnPrioritizedTasksInOrder() {
        Task t1 = new Task("Ранняя", "описание", Status.NEW,
                LocalDateTime.of(2024, 6, 1, 10, 0), Duration.ofMinutes(30));
        Task t2 = new Task("Поздняя", "описание", Status.NEW,
                LocalDateTime.of(2024, 6, 1, 11, 0), Duration.ofMinutes(30));

        manager.addTask(t2);
        manager.addTask(t1);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t1.getName(), prioritized.get(0).getName());
        assertEquals(t2.getName(), prioritized.get(1).getName());
    }

    @Test
    void shouldUpdateTask() {
        Task task = manager.addTask(new Task("До", "описание", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        task.setName("После");
        manager.updateTask(task);
        assertEquals("После", manager.getTask(task.getId()).getName());
    }

    @Test
    void shouldUpdateSubTaskAndReflectOnEpicStatus() {
        Epic epic = manager.addEpic(new Epic("Эпик", "Описание"));
        SubTask sub = manager.addSubTask(new SubTask("Sub", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10), epic.getId()));

        sub.setStatus(Status.DONE);
        manager.updateSubTask(sub);
        assertEquals(Status.DONE, manager.getSubTask(sub.getId()).getStatus());
        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldDeleteAllTasks() {
        manager.addTask(new Task("Task", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.deleteTasks();
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void shouldDeleteAllSubTasksAndUpdateEpic() {
        Epic epic = manager.addEpic(new Epic("Epic", "desc"));
        SubTask sub = manager.addSubTask(new SubTask("Sub", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15), epic.getId()));
        manager.deleteSubTasks();
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getEpic(epic.getId()).getIdSubTask().isEmpty());
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = manager.addTask(new Task("Task", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15)));
        manager.removeTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldUpdateEpicStatusCorrectly() {
        Epic epic = manager.addEpic(new Epic("Epic", "desc"));

        SubTask sub1 = new SubTask("Sub1", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Status.DONE,
                LocalDateTime.now(), Duration.ofMinutes(15), epic.getId());

        manager.addSubTask(sub1);
        manager.addSubTask(sub2);

        Status updated = manager.updateEpicStatus(epic.getId());
        assertEquals(Status.IN_PROGRESS, updated);
    }
}