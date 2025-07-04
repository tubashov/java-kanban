package taskmanager.http.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.model.Task;
import taskmanager.exceptions.NotFoundException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Обработчик HTTP-запросов для задач
public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager taskManager, Gson gson) {
       super(taskManager,gson);
    }

    // Обработка HTTP-запросов
//    @Override
//    public void handle(HttpExchange exchange) throws IOException {
//        try {
//            String method = exchange.getRequestMethod();        // Получаем HTTP-метод (GET, POST, DELETE)
//            URI requestURI = exchange.getRequestURI();
//            String query = requestURI.getQuery();               // Получаем строку параметров запроса
//
//            switch (method) {
//                case "GET":
//                    handleGet(exchange, query);                   // Обработка GET-запроса
//                    break;
//                case "POST":
//                    handlePost(exchange);                         // Обработка POST-запроса
//                    break;
//                case "DELETE":
//                    handleDelete(exchange, query);                 // Обработка DELETE-запроса
//                    break;
//                default:
//                    exchange.sendResponseHeaders(405, 0);         // 405 метод не поддерживается
//                    exchange.close();
//                    break;
//            }
//        } catch (JsonSyntaxException e) {
//            exchange.sendResponseHeaders(400, 0);  // 400 Bad Request при некорректном JSON
//            exchange.close();
//        } catch (IllegalArgumentException e) {
//            exchange.sendResponseHeaders(406, 0); // 406 пересечение задач
//            exchange.close();
//        } catch (NotFoundException e) {
//            sendNotFound(exchange, e.getMessage()); // 404 при отсутствии задачи
//        } catch (Exception e) {
//            sendServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage()); // 500 при других ошибках
//        }
//    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Обработка GET-запроса.
    @Override
    protected void processGet(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            Task task = manager.getTask(id);
            if (task == null) throw new NotFoundException("Задача с id=" + id + " не найдена");
            sendText(exchange, gson.toJson(task));              // Вернуть задачу как JSON
        } else {
            List<Task> tasks = manager.getTasks();
            sendText(exchange, gson.toJson(tasks));             // Вернуть список как JSON
        }
    }

    // Обработка POST-запроса.
    @Override
    protected void processPost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Task task = gson.fromJson(body, Task.class);
        if (task.getId() == null || manager.getTask(task.getId()) == null) {
            manager.addTask(task);  // Новая задачу, если нет id или она не найдена
        } else {
            manager.updateTask(task); // Обновление существующей задачи
        }
        sendCreated(exchange); // 201 — успешно создано/обновлено

    }

    // Обработка DELETE-запроса.
    @Override
    protected void processDelete(HttpExchange exchange, String query) throws IOException, NotFoundException {
        if (query != null && query.contains("id=")) {
            int id = Integer.parseInt(query.split("=")[1]);
            Task task = manager.getTask(id);
            if (task == null) throw new NotFoundException("Задача с id=" + id + " не найдена");
            manager.removeTask(id);
            sendText(exchange, "Задача с id=" + id + " удалена"); // Удалениет по id
        } else {
            manager.deleteTasks();
            sendText(exchange, "Все задачи удалены"); // Удаление всех задач
        }
    }
}
