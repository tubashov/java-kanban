package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.model.SubTask;
import taskmanager.exceptions.NotFoundException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Обработчик HTTP-запросов.
public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubTaskHandler(TaskManager taskManager, Gson gson) {
        this.manager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();    // Получаем HTTP-метод
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();           // Получаем параметры запроса

            switch (method) {
                case "GET":
                    handleGet(exchange, query);               // Обработка GET-запроса
                    break;
                case "POST":
                    handlePost(exchange);                     // Обработка POST-запроса
                    break;
                case "DELETE":
                    handleDelete(exchange, query);            // Обработка DELETE-запроса
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);     // Метод не поддерживается
                    exchange.close();
                    break;
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());         // Отправляем 404 при отсутствии объекта
        } catch (Exception e) {
            sendServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage()); // Общая ошибка сервера
        }
    }

    // Чтение тела запроса
    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Обработка GET-запроса.
    private void handleGet(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            SubTask subTask = manager.getSubTask(id);
            if (subTask == null) throw new NotFoundException("Подзадача с id=" + id + " не найдена");
            sendText(exchange, gson.toJson(subTask));        // Отправляем подзадачу в JSON
        } else {
            List<SubTask> subTasks = manager.getSubTasks();
            sendText(exchange, gson.toJson(subTasks));        // Отправляем список всех подзадач
        }
    }

    // Обработка POST-запроса.
    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        SubTask subTask = gson.fromJson(body, SubTask.class);
        if (subTask.getId() == null || manager.getSubTask(subTask.getId()) == null) {
            manager.addSubTask(subTask);                      // Создаем новую подзадачу
        } else {
            manager.updateSubTask(subTask);                   // Обновляем существующую подзадачу
        }
        sendCreated(exchange);                                 // Отправляем 201 Created
    }

    // Обработка DELETE-запроса.
    private void handleDelete(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            SubTask subTask = manager.getSubTask(id);
            if (subTask == null) throw new NotFoundException("Подзадача с id=" + id + " не найдена");
            manager.removeSubTask(id);
            sendText(exchange, "Подзадача с id=" + id + " удалена");
        } else {
            manager.deleteSubTasks();
            sendText(exchange, "Все подзадачи удалены");
        }
    }
}
