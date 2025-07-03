package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.model.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

// Обработчик для истории просмотра задач (History)
public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.manager = taskManager;
        this.gson = gson;
    }

    // Основной метод обработки запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Task> history = manager.getHistory();  // Получаем историю
                sendText(exchange, gson.toJson(history));   // Отправляем как JSON
            } else {
                // Метод не поддерживается
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        } catch (Exception e) {
            sendServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}

