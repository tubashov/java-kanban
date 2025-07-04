package taskmanager.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.exceptions.NotFoundException;
import taskmanager.model.SubTask;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubTaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager,gson);
    }

    // Основной метод обработки HTTP-запросов
//    @Override
//    public void handle(HttpExchange exchange) throws IOException {
//        try {
//            String method = exchange.getRequestMethod();
//            URI requestURI = exchange.getRequestURI();
//            String query = requestURI.getQuery();
//
//            switch (method) {
//                // Обработка GET-запроса
//                case "GET":
//                    handleGet(exchange, query);
//                    break;
//                // Обработка POST-запроса
//                case "POST":
//                    handlePost(exchange);
//                    break;
//                // Обработка DELETE-запроса
//                case "DELETE":
//                    handleDelete(exchange, query);
//                    break;
//                default:
//                    exchange.sendResponseHeaders(405, 0); // 405 — метод не поддерживается
//                    exchange.close();
//            }
//        } catch (JsonSyntaxException e) {
//            exchange.sendResponseHeaders(400, 0); // 400 — неверный JSON
//            exchange.close();
//        } catch (IllegalArgumentException e) {
//            exchange.sendResponseHeaders(406, 0); // 406 пересечение задач
//            exchange.close();
//        } catch (NotFoundException e) {
//            sendNotFound(exchange, e.getMessage()); // 404 — подзадача не найдена
//        } catch (Exception e) {
//            sendServerError(exchange, "Ошибка сервера: " + e.getMessage()); // 500 — внутренняя ошибка
//        }
//    }

    // Чтение тела запроса
    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Обработка GET-запроса
    @Override
    protected void processGet(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            SubTask subtask = manager.getSubTask(id); // Получить подзадачу по id
            if (subtask == null) {
                throw new NotFoundException("Подзадача с id=" + id + " не найдена");
            }
            sendText(exchange, gson.toJson(subtask)); // Вернуть подзадачу как JSON
        } else {
            List<SubTask> subtasks = manager.getSubTasks();
            sendText(exchange, gson.toJson(subtasks)); // Вернуть список как JSON
        }
    }

    // Обработка POST-запроса.
    @Override
    protected void processPost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        SubTask subtask = gson.fromJson(body, SubTask.class); // JSON в объект

        boolean isNew = subtask.getId() == null || manager.getSubTask(subtask.getId()) == null;

        try {
            if (isNew) {
                manager.addSubTask(subtask); //Новая подзадача
            } else {
                manager.updateSubTask(subtask); // Обновление существующей
            }
            sendCreated(exchange); // 201 — успешно создано/обновлено
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(406, 0);  // 406 — пересечение по времени
            exchange.close();
        }
    }

    // Обработка DELETE-запроса
    @Override
    protected void processDelete(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            SubTask subtask = manager.getSubTask(id);
            if (subtask == null) {
                throw new NotFoundException("Подзадача с id=" + id + " не найдена");
            }
            manager.removeSubTask(id); // Удаление по id
            sendText(exchange, "Подзадача с id=" + id + " удалена");
        } else {
            manager.deleteSubTasks(); // Удаление всех подзадач
            sendText(exchange, "Все подзадачи удалены");
        }
    }
}
