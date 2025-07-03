package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import taskmanager.controller.TaskManager;
import taskmanager.controller.Managers;
import taskmanager.http.adapter.DurationAdapter;
import taskmanager.http.adapter.LocalDateTimeAdapter;
import taskmanager.http.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;
    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // Конструктор по умолчанию — используется в main()
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault()); // делегируем в следующий конструктор
    }

    // Конструктор с внешним TaskManager — используется в тестах
    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TaskHandler(manager, gson));
        server.createContext("/epics", new EpicHandler(manager, gson));
        server.createContext("/subtasks", new SubTaskHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public void start() {
        System.out.println("HTTP Task Server started on port " + PORT);
        server.start();
    }

    public void stop() {
        System.out.println("Stopping HTTP Task Server...");
        server.stop(1);
    }

    public static Gson getGson() {
        return gson;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}