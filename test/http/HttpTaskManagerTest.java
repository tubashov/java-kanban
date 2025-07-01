package http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import taskmanager.controller.InMemoryTaskManager;
import taskmanager.controller.TaskManager;
import taskmanager.http.HttpTaskServer;
import taskmanager.model.Task;
import taskmanager.util.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpTaskManagerTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    @BeforeAll
    void init() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer();
        gson = HttpTaskServer.getGson(); // Получаем общий экземпляр Gson
        taskServer.start();
    }

    @BeforeEach
    void setUp() {
        manager.deleteTasks(); // очищаем менеджер перед каждым тестом
   }

    @AfterAll
    void tearDown() {
        taskServer.stop();     // останавливаем сервер после теста
    }

    @Test
    void testAddTaskSuccessfully() throws IOException, InterruptedException {
        // Создаём задачу
        Task task = new Task(1, "Test Task", "Descriprion", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0),
                Duration.ofMinutes(60));
        manager.addTask(task);

        // Преобразуем в JSON
        String json = gson.toJson(task);

        // Отправляем POST-запрос
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус ответа
        assertEquals(201, response.statusCode(), "Ожидался статус 201 Created");

        // Проверяем, что задача добавлена в менеджер
        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "Ожидалась одна задача");
        assertEquals("Test Task", tasks.get(0).getName(), "Имя задачи не совпадает");
    }

    @Test
    void testAddTaskInvalidJson() throws IOException, InterruptedException {
        // Отправляем некорректный JSON
        String invalidJson = "{ invalid json }";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что сервер ответил с 400 Bad Request
        assertEquals(400, response.statusCode(), "Ожидался статус 400 при неверном JSON");
    }
}

