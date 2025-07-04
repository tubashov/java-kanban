package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.model.Epic;
import taskmanager.exceptions.NotFoundException;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Обработчик HTTP-запросов
public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskManager taskManager, Gson gson) {
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
            Epic epic = manager.getEpic(id);
            if (epic == null) throw new NotFoundException("Эпик с id=" + id + " не найден");
            sendText(exchange, gson.toJson(epic));         // Отправляем эпик в JSON
        } else {
            List<Epic> epics = manager.getEpics();
            sendText(exchange, gson.toJson(epics));         // Отправляем список всех эпиков
        }
    }

    // Обработка POST-запроса.
    @Override
    protected void processPost(HttpExchange exchange) throws IOException {
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
    @Override
    protected void processDelete(HttpExchange exchange, String query) throws IOException, NotFoundException {
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
