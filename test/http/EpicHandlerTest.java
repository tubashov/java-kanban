package http;

import org.junit.jupiter.api.*;
import taskmanager.controller.InMemoryTaskManager;
import taskmanager.model.Epic;
import taskmanager.model.SubTask;
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
public class EpicHandlerTest extends BaseHttpTest {

    private static final String ENDPOINT1 = "/epics";
    private static final String ENDPOINT2 = "/subtasks";

    @BeforeEach
    void clearEpics() {
        manager.deleteEpics();
    }

    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(null, "Epic 1", "Description", Status.NEW);
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Ожидался статус 201 Created");

        Epic createdEpic = manager.getEpics().get(0);
        assertEquals("Epic 1", createdEpic.getName());
        assertEquals("Description", createdEpic.getDescription());
        assertEquals(Status.NEW, createdEpic.getStatus());
    }

    @Test
    void shouldGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic(null, "Epic 1", "Description", Status.NEW);

        String json = gson.toJson(epic);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        manager.addEpic(epic);

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size());
        Epic added = epics.get(0);
        int id = added.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic[] responseEpics = gson.fromJson(response.body(), Epic[].class);
        assertNotNull(responseEpics);
        assertEquals(1, responseEpics.length);
        assertEquals(id, responseEpics[0].getId());
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic(null, "Epic 1", "Description", Status.NEW);
        String json = gson.toJson(epic);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(),
                "Ожидался статус 201 Created при добавлении эпика");

        int id = manager.getEpics().get(0).getId();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1 + id))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(),
                "Ожидался статус 200 OK при получении существующего эпика");
        assertTrue(getResponse.body().contains("Epic 1"), "Ответ должен содержать название эпика");

        // Попытка получить несуществующий эпик
        HttpRequest notFoundRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1 + "?id=9999"))
                .GET()
                .build();

        HttpResponse<String> notFoundResponse = client.send(notFoundRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, notFoundResponse.statusCode(), "Ожидался статус 404 при запросе несуществующего эпика");
    }

        @Test
    void shouldGetEpicSubTasks() throws IOException, InterruptedException {
        Epic epic = new Epic(null, "Epic 1", "Description", Status.NEW);
        String epicJson = gson.toJson(epic);

        HttpRequest epicPostRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicPostResponse = client.send(epicPostRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicPostResponse.statusCode(), "Ожидался статус 201 при создании эпика");

        // Получаем id эпика
        int epicId = manager.getEpics().get(0).getId();

        // Добавление подзадачи к эпику
        SubTask subTask = new SubTask(null, "Subtask 1","Description", Status.NEW,
                LocalDateTime.of(2025, 6, 16, 10, 0),
                Duration.ofMinutes(30), epicId
        );
        String subTaskJson = gson.toJson(subTask);

        HttpRequest subPostRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();

        HttpResponse<String> subPostResponse = client.send(subPostRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subPostResponse.statusCode(), "Ожидался статус 201 при создании сабтаска");

        // Получение подзадач по epicId
        HttpRequest getSubtasksRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1 + epicId + ENDPOINT2))
                .GET()
                .build();

        HttpResponse<String> getSubtasksResponse = client.send(getSubtasksRequest,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getSubtasksResponse.statusCode(),
                "Ожидался статус 200 при запросе сабтасков");

        SubTask[] subtasks = gson.fromJson(getSubtasksResponse.body(), SubTask[].class);
        assertNotNull(subtasks, "Список сабтасков не должен быть null");
        assertEquals(1, subtasks.length, "Ожидался один сабтаск");

        // Запрос подзадач по несуществующему epicId
        HttpRequest invalidRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1 + "?id=9999"))
                .GET()
                .build();

        HttpResponse<String> invalidResponse = client.send(invalidRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, invalidResponse.statusCode(),
                "Ожидался статус 404 при запросе сабтасков несуществующего эпика");
    }

    @Test
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic(null, "Epic 1","Description", Status.NEW);
        String json = gson.toJson(epic);

        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.send(post, HttpResponse.BodyHandlers.ofString());

        int id = manager.getEpics().get(0).getId();

        HttpRequest delete = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ENDPOINT1 + "?id=" + id))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(delete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getEpic(id));
    }
}
