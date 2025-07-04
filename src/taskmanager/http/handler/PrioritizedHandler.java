package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.model.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

// Обработчик для получения задач, отсортированных по приоритету
public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager,gson);
    }

    // Обработка HTTP-запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Task> prioritizedTasks = manager.getPrioritizedTasks(); // Получение отсортированного списка
                sendText(exchange, gson.toJson(prioritizedTasks));           // Отправка JSON-ответа
            } else {
                // Метод не поддерживается - отправляем 405
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }

        } catch (Exception e) {
            sendServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage()); // Обработка ошибок
        }
    }
}
