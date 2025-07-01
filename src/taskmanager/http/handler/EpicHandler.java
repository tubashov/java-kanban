package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.model.Epic;
import taskmanager.exceptions.NotFoundException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Обработчик HTTP-запросов
public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        this.manager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();    // Получаем метод запроса
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
            sendNotFound(exchange, e.getMessage());         // Обработка ошибки — объект не найден
        } catch (Exception e) {
            sendServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage()); // Общая ошибка
        }
    }

    // Чтение тела запроса в строку
    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Обработка GET-запроса.
    private void handleGet(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            Epic epic = manager.getEpic(id);
            if (epic == null) throw new NotFoundException("Эпик с id=" + id + " не найден");
            sendText(exchange, gson.toJson(epic));         // Отправляем эпик в JSON
        } else {
            List<Epic> epics = manager.getEpics();
            sendText(exchange, gson.toJson(epics));         // Отправляем список всех эпиков
        }
    }

    // Обработка POST-запроса.
    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);
        if (epic.getId() == null || manager.getEpic(epic.getId()) == null) {
            manager.addEpic(epic);                          // Создаем новый эпик
        } else {
            manager.updateEpic(epic);                       // Обновляем существующий эпик
        }
        sendCreated(exchange);                              // Отправляем 201 Created
    }

    // Обработка DELETE-запроса.
    private void handleDelete(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            Epic epic = manager.getEpic(id);
            if (epic == null) throw new NotFoundException("Эпик с id=" + id + " не найден");
            manager.removeEpic(id);
            sendText(exchange, "Эпик с id=" + id + " удалён");
        } else {
            manager.deleteEpics();
            sendText(exchange, "Все эпики удалены");
        }
    }
}
