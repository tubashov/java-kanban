import taskmanager.controller.*;
import taskmanager.model.*;
import taskmanager.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Main {
    public static void main(String[] args) {
        try {
            // Создание временного файла
            File file = File.createTempFile("task_manager", ".csv");

            // Создание менеджера с привязкой к файлу
            FileBackedTaskManager manager = new FileBackedTaskManager(file, Charset.defaultCharset());

            // Создание задач
            Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
            Task task2 = new Task("Задача 2", "Описание задачи 2", Status.IN_PROGRESS);
            manager.addTask(task1);
            manager.addTask(task2);

            Epic epic = new Epic("Эпик 1", "Описание эпика", Status.NEW);
            manager.addEpic(epic);

            SubTask sub1 = new SubTask("Подзадача 1", "К подзадаче 1", Status.NEW, epic.getId());
            SubTask sub2 = new SubTask("Подзадача 2", "К подзадаче 2", Status.DOWN, epic.getId());
            manager.addSubTask(sub1);
            manager.addSubTask(sub2);

            // Вызов задач для добавления в историю
            manager.getTask(task1.getId());
            manager.getEpic(epic.getId());
            manager.getSubTask(sub2.getId());

            System.out.println("Оригинальные задачи:");
            printAllTasks(manager);

            // Восстановление менеджера из файла
            System.out.println("\nВосстановленные задачи:");
            FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(file);
            printAllTasks(restoredManager);

        } catch (IOException e) {
            System.err.println("Ошибка при работе с временным файлом: " + e.getMessage());
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);
            for (Task sub : manager.getSubTaskList(epic.getId())) {
                System.out.println("  --> " + sub);
            }
        }

        System.out.println("Подзадачи:");
        for (Task sub : manager.getSubTasks()) {
            System.out.println(sub);
        }

        System.out.println("История:");
        for (Task t : manager.getHistory()) {
            System.out.println(t);
        }
    }
}

