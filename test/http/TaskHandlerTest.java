package http;

import org.junit.jupiter.api.*;
import taskmanager.controller.InMemoryTaskManager;
import taskmanager.model.Task;
import taskmanager.util.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskHandlerTest extends BaseHttpTest {

    @BeforeEach
    void clearTasks() {
        manager.deleteTasks();
    }

    @Test
    void shouldAddOrUpdateTask() throws IOException, InterruptedException {
        Task task = new Task(null, "Task 1", "Description", Status.NEW,
                LocalDateTime.of(2025, 6, 15, 12, 0),
                Duration.ofMinutes(30));
        String json = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode(), "При создании задачи ожидался статус 201");

        // Обновление задачи — update
        Task existing = manager.getTasks().get(0);
        existing.setName("Task 1 updated");
        String updatedJson = gson.toJson(existing);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode(), "При обновлении задачи ожидался статус 201");

        // Пересекающаяся по времени задача
        Task conflictingTask = new Task(null, "Task 2", "Conflict", Status.NEW,
                LocalDateTime.of(2025, 6, 15, 12, 15),
                Duration.ofMinutes(20));
        String conflictJson = gson.toJson(conflictingTask);

        HttpRequest conflictRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(conflictJson))
                .build();

        HttpResponse<String> conflictResponse = client.send(conflictRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, conflictResponse.statusCode(), "При пересечении задач ожидался статус 406");
    }

    @Test
    void shouldGetTasks() throws IOException, InterruptedException {
        Task task = new Task(null, "Test 1", "Description", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0),
                Duration.ofMinutes(30));
        manager.addTask(task);

        List<Task> allTasks = manager.getTasks();
        assertEquals(1, allTasks.size(), "Ожидалась одна задача в менеджере");
        Task addedTask = allTasks.get(0);
        int id = addedTask.getId();

        // GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверка: статус 200 OK
        assertEquals(200, response.statusCode());

        // Десериализация ответа
        Task[] tasks = gson.fromJson(response.body(), Task[].class);

        // Проверки содержимого
        assertNotNull(tasks, "Список задач не должен быть null");
        assertEquals(1, tasks.length, "Ожидалась одна задача в ответе");

        Task received = tasks[0];
        assertEquals(id, received.getId(), "ID задачи должен совпадать");
        assertEquals("Test 1", received.getName(), "Имя задачи должно совпадать");
        assertEquals("Description", received.getDescription(), "Описание должно совпадать");
        assertEquals(Status.NEW, received.getStatus(), "Статус должен совпадать");
        assertEquals(LocalDateTime.of(2025, 6, 16, 10, 0), received.getStartTime(), "Время начала должно совпадать");
        assertEquals(Duration.ofMinutes(30), received.getDuration(), "Продолжительность должна совпадать");
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task(null, "Task 1", "Description", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 11, 0),
                Duration.ofMinutes(30));

        // Сериализация задачи в JSON
        String json = gson.toJson(task);

        // POST-запрос для добавления задачи
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Ожидался статус 201 Created");

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "Ожидалась одна задача");

        int taskId = tasks.get(0).getId();

        // GET-запрос
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks?id=" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Ожидался статус 200 OK");
        assertTrue(getResponse.body().contains("Task 1"), "Ответ должен содержать имя задачи");

        // Попытка получить несуществующую задачу
        HttpRequest notFoundRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks?id=9999"))
                .GET()
                .build();

        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, notFoundResponse.statusCode(), "Ожидался статус 404 при запросе несуществующей задачи");
    }


    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task(null, "Task 1", "Description", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0),
                Duration.ofMinutes(60));
        String json = gson.toJson(task);

        // POST-запрос для создания задачи
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Ожидался статус 201 Created");

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "Ожидалась одна задача");
        int taskId = tasks.get(0).getId();

        // DELETE-запрос для удаления задачи
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks?id=" + taskId))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode(), "Ожидался статус 200 OK");

        // Задача действительно удалена
        assertNull(manager.getTask(taskId), "Задача должна быть удалена");
    }

    @Test
    void testAddTaskInvalidJson() throws IOException, InterruptedException {
        // Некорректный JSON
        String invalidJson = "{ invalid json }";

        // POST-запрос с некорректным JSON
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL+ "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        // Отправляем запрос и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что сервер вернул 400 Bad Request
        assertEquals(400, response.statusCode(), "Ожидался статус 400 при неверном JSON");
    }
}
