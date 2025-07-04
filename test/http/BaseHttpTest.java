package http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import taskmanager.controller.InMemoryTaskManager;
import taskmanager.controller.TaskManager;
import taskmanager.http.HttpTaskServer;

import java.io.IOException;
import java.net.http.HttpClient;

// Cервер, клиент, менеджер, gson.
public abstract class BaseHttpTest {

    protected static HttpTaskServer server;
    protected static TaskManager manager;
    protected static Gson gson;
    protected static HttpClient client;
    protected static    final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void start() throws IOException {
        manager = new InMemoryTaskManager();         // InMemoryTaskManager
        gson = HttpTaskServer.getGson();             // Объект Gson для сериализации/десериализации
        client = HttpClient.newHttpClient();         // HTTP-клиент для отправки запросов
        server = new HttpTaskServer(manager);        // Запуск сервера
        server.start();
    }

    @AfterAll
    static void stop() {
        server.stop();                               // Останавить сервер после всех тестов
    }
}

