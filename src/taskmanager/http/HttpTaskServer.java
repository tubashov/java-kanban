package taskmanager.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import taskmanager.controller.TaskManager;
import taskmanager.controller.Managers;
import taskmanager.http.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        // Получаем менеджер задач
        this.manager = Managers.getDefault();
        this.gson = new Gson();

        // Создаем HTTP сервер на порту 8080
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрируем обработчики на соответствующие пути
        server.createContext("/tasks/task", new TaskHandler(manager, gson));
        server.createContext("/tasks/subtask", new SubTaskHandler(manager, gson));
        server.createContext("/tasks/epic", new EpicHandler(manager, gson));
        server.createContext("/tasks/history", new HistoryHandler(manager, gson));
        server.createContext("/tasks", new PrioritizedHandler(manager, gson));
    }

    // Запуск сервер
    public void start() {
        System.out.println("HTTP Task Server started on port " + PORT);
        server.start();
    }

    // Остановка сервера
    public void stop() {
        server.stop(1);
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}
