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

    // добавление задачи
    @Test
    void shouldAddTaskSuccessfully() {
        Task task = new Task(1, "Тестовая задача", "Описание", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0),
                Duration.ofMinutes(60));

        Task addedTask = manager.addTask(task);

        assertNotNull(addedTask.getId(), "ID должен быть присвоен задаче");
        Task retrievedTask = manager.getTask(addedTask.getId());
        assertEquals(task.getName(), retrievedTask.getName(), "Имя задачи должно совпадать");
        assertEquals(task.getDescription(), retrievedTask.getDescription(), "Описание должно совпадать");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус должен совпадать");
    }

    // добавление эпика
    @Test
    void shouldAddEpicAndGetItById() {
        Epic epic = new Epic(1, "Эпик 1", "Тест-эпик 1", Status.NEW);
        Epic addedEpic = manager.addEpic(epic);

        assertNotNull(addedEpic.getId());
        Epic retrieved = manager.getEpic(addedEpic.getId());

        assertEquals(epic.getName(), retrieved.getName());
        assertEquals(epic.getDescription(), retrieved.getDescription());
    }

    // добавление подзадачи и проверка наличия связанного эпика
    @Test
    void shouldAddSubTaskAndGetItById() {
        Epic epic = manager.addEpic(new Epic(1, "Эпик 1", "Тест-эпик 1", Status.NEW));
        SubTask sub = new SubTask(1, "Подзадача 1", "Тест-подзадача 1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30), epic.getId());
        SubTask addedSub = manager.addSubTask(sub);

        assertNotNull(addedSub.getId());
        SubTask retrieved = manager.getSubTask(addedSub.getId());
        assertEquals(sub.getName(), retrieved.getName());
        assertEquals(epic.getId(), retrieved.getEpicId());
    }

    // добавление задачи, эпика, подзадачи
    @Test
    void shouldReturnAllTasksAndEpicsAndSubTasks() {
        Task task = new Task(1, "Задача 1", "Тест-задача 1", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 0),
                Duration.ofMinutes(10));
        manager.addTask(task);
        manager.addEpic(new Epic(1, "Эпик 1", "Тест-эпик 1", Status.NEW));
        manager.addSubTask(new SubTask(1, "Подзадача 1", "Тест-подзадача 1", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 30),
                Duration.ofMinutes(10)));

        assertEquals(1, manager.getTasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(1, manager.getSubTasks().size());
    }

    // добавление эпика без подзадач
    @Test
    void shouldReturnEmptyListIfNoSubTasksInEpic() {
        Epic epic = manager.addEpic(new Epic(1, "Эпик без подзадач", "Тест-эпик без подзадач",
                Status.NEW));
        assertTrue(manager.getSubTaskList(epic.getId()).isEmpty());
    }

    // упорядоченность задач по времени
    @Test
    void shouldReturnPrioritizedTasksInOrder() {
        Task t1 = new Task(1, "Задача 1", "Первичная", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0), Duration.ofMinutes(30));
        Task t2 = new Task(2, "Задача 2", "Последующая", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 11, 0), Duration.ofMinutes(30));

        manager.addTask(t2);
        manager.addTask(t1);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t1.getName(), prioritized.get(0).getName());
        assertEquals(t2.getName(), prioritized.get(1).getName());
    }

    // проверка расчета при пересечении временных интервалов
    @Test
    void shouldDetectedOverlapping() {
        Task t1 = new Task(1, "Задача 1", "Первичная", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0), Duration.ofMinutes(60));
        Task t2 = new Task(2, "Задача 2", "Последующая", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 30), Duration.ofMinutes(60));

        manager.addTask(t1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manager.addTask(t2));

        assertEquals("Ошибка: задача пересекается по времени.", exception.getMessage());
    }

    // проверка расчета при непересечении временных интервалов
    @Test
    void shouldAllowNonOverlapping() {
        Task t1 = new Task(1, "Задача 1", "Первичная", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0), Duration.ofMinutes(60));
        Task t2 = new Task(2, "Задача 2", "Последующая", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 11, 0), Duration.ofMinutes(60));

        manager.addTask(t1);
        assertDoesNotThrow(() -> manager.addTask(t2));
    }

    // изменение задачи
    @Test
    void shouldUpdateTask() {
        Task task = manager.addTask(new Task(1, "Задача 1", "Новая", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        task.setDescription("Измененная");
        manager.updateTask(task);
        assertEquals("Измененная", manager.getTask(task.getId()).getDescription());
    }

    // расчет статуса эпика на основании статуса подзадачи
    @Test
    void shouldUpdateSubTaskAndReflectOnEpicStatus() {
        Epic epic = manager.addEpic(new Epic(1, "Эпик 1", "Тест-эпик статус", Status.NEW));

        SubTask sub = new SubTask(null, "Подзадача 1", "Тест-подзадача 1",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epic.getId());
        sub = manager.addSubTask(sub); // подзадача зарегистрирована

        sub.setStatus(Status.DONE);
        manager.updateSubTask(sub); // изменение статуса в менеджере

        assertEquals(Status.DONE, manager.getSubTask(sub.getId()).getStatus());
        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    // удаление всех задач
    @Test
    void shouldDeleteAllTasks() {
        manager.addTask(new Task(1, "Задача 1", "Тест-задача удаление", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.deleteTasks();
        assertTrue(manager.getTasks().isEmpty());
    }

    // удаление подзадач
    @Test
    void shouldDeleteAllSubTasksAndUpdateEpic() {
        Epic epic = manager.addEpic(new Epic(1, "Эпик 1", "Тест-эпик 1", Status.NEW));
        SubTask sub = manager.addSubTask(new SubTask(1, "Подзадача", "Тест-подзадача удаление",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(15), epic.getId()));
        manager.deleteSubTasks();
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getEpic(epic.getId()).getIdSubTask().isEmpty());
    }

    // удаление задачи по id
    @Test
    void shouldDeleteTaskById() {
        Task task = manager.addTask(new Task(1, "Задача 1", "Тест-задача 1 удаление", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15)));
        manager.removeTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    // обновление статуса эпика при изменении статуса задачи
    @Test
    void shouldUpdateEpicStatusCorrectly() {
        Epic epic = manager.addEpic(new Epic(1, "Эпик 1", "Тест-эпик статус", Status.NEW));

        SubTask sub1 = new SubTask(1, "Подзадача 1", "Тест-подзадача завершенная", Status.DONE,
                LocalDateTime.of(2025, 6, 20, 18, 00),
                Duration.ofMinutes(10), epic.getId());
        SubTask sub2 = new SubTask(1, "Подзадача 1", "Тест-подзадача новая", Status.NEW,
                LocalDateTime.of(2025, 6, 20, 18, 30),
                Duration.ofMinutes(10), epic.getId());

        manager.addSubTask(sub1);
        manager.addSubTask(sub2);

        Status updated = manager.updateEpicStatus(epic.getId());
        assertEquals(Status.IN_PROGRESS, updated);
    }
}