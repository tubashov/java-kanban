import taskmanager.controller.*;
import taskmanager.model.*;
import taskmanager.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        try {
            File file = File.createTempFile("task_manager", ".csv");

            FileBackedTaskManager manager = new FileBackedTaskManager(file, Charset.defaultCharset());

            // Примерная дата и продолжительность
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 0);
            Duration duration = Duration.ofMinutes(90);

            Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW, now, duration);
            Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.IN_PROGRESS, now.plusHours(2), duration);
            manager.addTask(task1);
            manager.addTask(task2);

            Epic epic = new Epic(3, "Эпик 1", "Описание эпика", Status.NEW);
            manager.addEpic(epic);

            SubTask sub1 = new SubTask(4, "Подзадача 1", "К подзадаче 1", Status.NEW,
                    now.plusHours(1), Duration.ofMinutes(60), epic.getId());
                SubTask sub2 = new SubTask(5, "Подзадача 2", "К подзадаче 2", Status.DONE,
                    now.plusHours(3), Duration.ofMinutes(45), epic.getId());
            manager.addSubTask(sub1);
            manager.addSubTask(sub2);

            // Вызовы для добавления в историю
            manager.getTask(task1.getId());
            manager.getEpic(epic.getId());
            manager.getSubTask(sub2.getId());

            System.out.println("Оригинальные задачи:");
            printAllTasks(manager);

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
