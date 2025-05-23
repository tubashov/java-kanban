package taskmanager.controller;

import taskmanager.model.Task;
import taskmanager.model.Epic;
import taskmanager.model.SubTask;
import taskmanager.util.Status;

import java.util.ArrayList;

public interface TaskManager {
    int generateId();

    Task addTask(Task task);

    Epic addTask(Epic epic);

    SubTask addTask(SubTask subTask);

    // получение списка всех задач
    ArrayList<taskmanager.model.Task> getTasks();

    ArrayList<SubTask> getSubTasks();

    ArrayList<Epic> getEpics();

    // получение по идентификатору

    Task getTask(int idTask);

    Epic getEpic(int idEpic);

    SubTask getSubTask(int idSubTask);

    // список подзадач определенного эпика
    ArrayList<SubTask> getSubTaskList(int epicId);

    // обновление задачи
    void updateTask(taskmanager.model.Task task);

    void updateEpic(Epic epic);

    void updateSubTask(SubTask subTask);

    void getHistory();

    // удаление всех задач
    void deleteTasks();

    void deleteEpics();

    void deleteSubTasks();

    // удаление по идентификатору
    void removeTask(int id);

    void removeEpic(int epicId);

    void removeSubTask(int id);

    // изменение статуса эпика
    Status updateEpicStatus(int epicId);
}
