package http;

import org.junit.jupiter.api.*;
import taskmanager.model.Epic;
import taskmanager.model.SubTask;
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
public class SubTaskHandlerTest extends BaseHttpTest {

    private static final String ENDPOINT = "/subtasks";

    @BeforeEach
    void setup() {
        manager.deleteSubTasks();
        manager.deleteEpics();
    }

    @Test
    void shouldAddOrUpdateSubTask() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(new Epic(null, "Epic 1", "Desription", Status.NEW));
        SubTask subTask = new SubTask(null, "SubTask 1", "Desription", Status.NEW,
                LocalDateTime.of(2025, 6, 15, 10, 0),
                Duration.ofMinutes(30),
                epic.getId());
        String json = gson.toJson(subTask);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode(), "При создании задачи ожидался статус 201");

        // Обновление подзадачи — update
        Task existing = manager.getSubTasks().get(0);
        existing.setName("SubTask 1 updated");
        String updatedJson = gson.toJson(existing);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode(), "При обновлении подзадачи ожидался статус 201");

        // Пересекающаяся по времени подзадача
        SubTask conflictingTask = new SubTask(null, "SubTask 2", "Conflict", Status.NEW,
                LocalDateTime.of(2025, 6, 15, 10, 15),
                Duration.ofMinutes(20));
        String conflictJson = gson.toJson(conflictingTask);

        HttpRequest conflictRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(conflictJson))
                .build();

        HttpResponse<String> conflictResponse = client.send(conflictRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, conflictResponse.statusCode(), "При пересечении подзадач ожидался статус 406");
    }

    @Test
    void shouldGetSubTasks() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(new Epic(null, "Epic 1", "Desription", Status.NEW));
        manager.addSubTask(new SubTask(null, "SubTask 1", "", Status.NEW,
                LocalDateTime.of(2025, 6, 15, 10, 15),
                Duration.ofMinutes(20),
                epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        SubTask[] subtasks = gson.fromJson(response.body(), SubTask[].class);
        assertEquals(1, subtasks.length);
    }

    @Test
    void shouldGetSubTaskById() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(new Epic(null, "Epic 1", "Desription", Status.NEW));
        SubTask created = manager.addSubTask(new SubTask(null, "SubTask 1", "Description",
                Status.NEW,
                LocalDateTime.of(2025, 7, 3, 11, 0),
                Duration.ofMinutes(40),
                epic.getId()));

        int id = created.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT + "?id=" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        SubTask result = gson.fromJson(response.body(), SubTask.class);
        assertEquals("SubTask 1", result.getName());

        HttpRequest notFoubdRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT + "?id=9999"))
                .GET()
                .build();

        HttpResponse<String> notFoundResponse = client.send(notFoubdRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, notFoundResponse.statusCode());
    }

    @Test
    void shouldDeleteSubTaskById() throws IOException, InterruptedException {
        Epic epic = manager.addEpic(new Epic(null, "Epic 1", "Desription", Status.NEW));
        SubTask subTask = new SubTask(null, "SubTask 1", "Desription", Status.NEW,
                LocalDateTime.of(2025, 6, 15, 10, 0),
                Duration.ofMinutes(30),
                epic.getId());
        String json = gson.toJson(subTask);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode(), "При создании задачи ожидался статус 201");

        List<SubTask> subTasks = manager.getSubTasks();
        assertEquals(1, subTasks.size(), "Ожидалась одна задача");
        int subTaskId = subTasks.get(0).getId();

        // DELETE-запрос для удаления задачи
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT + "?id=" + subTaskId))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode(), "Ожидался статус 200 OK");

        // Задача действительно удалена
        assertNull(manager.getTask(subTaskId), "Задача должна быть удалена");
    }
}
