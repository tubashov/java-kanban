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

    // Чтение тела запроса в строку
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
