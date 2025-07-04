package taskmanager.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.exceptions.NotFoundException;
import taskmanager.model.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Обработчик HTTP-запросов для задач
public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubTaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager,gson);
    }

    // Чтение тела запроса в строку
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
