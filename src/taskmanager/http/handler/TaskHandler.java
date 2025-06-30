package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.model.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Обработка HTTP-запросов, связанные с обычными задачами (Task).
public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;
    private final Gson gson = new Gson();  // Библиотека Gson для сериализации/десериализации JSON

    public TaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
    }

    // Обработка HTTP-запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET":
                handleGet(exchange); // Обработка получения задач
                break;
            case "POST":
                handlePost(exchange); // Обработка добавления новой задачи
                break;
            case "DELETE":
                handleDelete(exchange); // Обработка удаления всех задач
                break;
            default:
                // Если метод не поддерживается, возвращаем 405 (Method Not Allowed)
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
        }
    }

    // Обработка получения задач
    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getTasks();
        String response = gson.toJson(tasks);
        sendText(exchange, response);
    }

    // Обработка добавления задачи
    private void handlePost(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Task task = gson.fromJson(reader, Task.class);
            taskManager.addTask(task);
            exchange.sendResponseHeaders(201, 0);
        }
        exchange.close(); // Завершаем обмен
    }

    // Обработка удаления задач
    private void handleDelete(HttpExchange exchange) throws IOException {
        taskManager.deleteTasks();
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
}
