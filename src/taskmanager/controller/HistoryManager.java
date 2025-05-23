package taskmanager.controller;

import taskmanager.model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task); // добавление задачи как просмотренной

    List<Task> getHistory(); // возвращает список задач

    void remove(int id); // удаление задачи из истории просмотра
}
