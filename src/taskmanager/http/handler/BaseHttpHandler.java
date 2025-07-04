package taskmanager.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.controller.TaskManager;
import taskmanager.exceptions.NotFoundException;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    protected final TaskManager manager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod(); // HTTP-метод (GET, POST, DELETE)
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();        // Строка запроса

            switch (method) {
                case "GET":
                    processGet(exchange, query);
                    break;
                case "POST":
                    processPost(exchange);
                    break;
                case "DELETE":
                    processDelete(exchange, query);
                    break;
                default:
                    sendMethodNotAllowed(exchange); // 405 - метод не поддерживается
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Некорректный JSON: " + e.getMessage()); // 400
        } catch (IllegalArgumentException e) {
            sendNotAcceptable(exchange, "Некорректные данные: " + e.getMessage()); // 406
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage()); // 404 - не найдено
        } catch (Exception e) {
            sendServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage()); // 500
        }
    }

    // методы-заглушки
    protected void processGet(HttpExchange exchange, String query) throws IOException, NotFoundException {
        sendMethodNotAllowed(exchange); // по умолчанию 405
    }

    protected void processPost(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange); // по умолчанию 405
    }

    protected void processDelete(HttpExchange exchange, String query) throws IOException, NotFoundException {
        sendMethodNotAllowed(exchange); // по умолчанию 405
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, 0);
        exchange.close();
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(400, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(404, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendNotAcceptable(HttpExchange exchange, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(406, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendServerError(HttpExchange exchange, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(500, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, 0);
        exchange.close();
    }
}
