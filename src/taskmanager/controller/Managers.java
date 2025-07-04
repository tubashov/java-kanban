package taskmanager.controller;

public class Managers {

    // Метод, который возвращает TaskManager
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    // Метод, который возвращает HistoryManager
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
