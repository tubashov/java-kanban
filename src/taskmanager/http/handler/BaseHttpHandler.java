package taskmanager.http.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Общий обработчик
public class BaseHttpHandler {

    // Ответ клиенту со статусом 200 (OK).
    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close(); // обязательно закрываем соединение
    }

    // Сообщение об ошибке 404 — Not Found (ресурс не найден).
     protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    // Сообщение об ошибке 406 — Not Acceptable (например, при пересечении задач).
    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(406, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    // Сообщение об ошибке 500 — Internal Server Error (внутренняя ошибка сервера).
    protected void sendServerError(HttpExchange exchange, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(500, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }
}
