package taskmanager.controllers;

import taskmanager.model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task); // отметка задачи как просмотренной

    List<Task> getHistory(); // возвращает список задач

    void remove(int id);
}
