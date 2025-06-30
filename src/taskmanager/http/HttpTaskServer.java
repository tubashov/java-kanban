package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import taskmanager.controller.*;
import taskmanager.http.handler.EpicHandler;
import taskmanager.http.handler.HistoryHandler;
import taskmanager.http.handler.TaskHandler;
import taskmanager.http.handler.*;


import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager manager;
    private final Gson gson;
    private final HttpServer server;

    // Встроенный HTTP-сервер
    public HttpTaskServer() throws IOException {
        this.manager = new InMemoryTaskManager(); // Инициализация менеджера
        this.gson = new GsonBuilder()
                .serializeNulls()
                .create(); // Создание Gson с сериализацией null

        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрация обработчиков для разных путей
        server.createContext("/tasks", new TaskHandler(manager, gson));
    }

    // Запуск сервера
    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    // Остановка сервера
    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен.");
    }

    // IOException могут сгенерировать методы create() и bind(...)
    public static void main(String[] args) throws IOException {
        // создание http-сервера
        new HttpTaskServer().start();
    }
}
