package taskmanager.controller;

import taskmanager.exceptions.ManagerSaveException;
import taskmanager.model.*;
import taskmanager.util.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private final Charset charset;

    public FileBackedTaskManager(File file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    // сохранение задач в файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), charset))) {
            writer.write("id,type,name,status,description,id epic\n");

            for (Task task : getTasks()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            for (Epic epic : getEpics()) {
                writer.write(taskToString(epic));
                writer.newLine();
            }
            for (SubTask subTask : getSubTasks()) {
                writer.write(taskToString(subTask));
                writer.newLine();
            }

            writer.newLine(); // пустая строка перед историей
            writer.write(historyToString(getHistoryManager()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении задач в файл", e);
        }
    }

    // сохранение задачи в строку
    public static String taskToString(Task task) {
        String epicId = "";
        TaskType type = task.getType();

        if (type == TaskType.SUBTASK) {
            epicId = String.valueOf(((SubTask) task).getEpicId());
        }

        return task.getId() + "," +
                type + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                epicId;
    }

    // создание задачи из строки
    public static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new SubTask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    // загрузка данных из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean readingTasks = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    readingTasks = false;
                    continue;
                }

                if (line.startsWith("id,")) continue; // пропустить заголовок

                if (readingTasks) {
                    Task task = fromString(line);
                    switch (task.getType()) {
                        case TASK -> manager.addTask(task);
                        case EPIC -> manager.addEpic((Epic) task);
                        case SUBTASK -> manager.addSubTask((SubTask) task);
                    }
                } else {
                    // Чтение истории
                    String[] ids = line.split(",");
                    for (String idStr : ids) {
                        if (!idStr.isBlank()) {
                            int id = Integer.parseInt(idStr);
                            Task task = manager.getTaskById(id);
                            if (task != null) {
                                manager.getHistoryManager().add(task);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    // сохранение истории в строку
    private static String historyToString(HistoryManager manager) {
        StringBuilder sb = new StringBuilder();
        for (Task task : manager.getHistory()) {
            sb.append(task.getId()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // удалить последнюю запятую
        }
        return sb.toString();
    }

    // Поиск задачи по ID (нужен для восстановления истории)
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) return tasks.get(id);
        if (epics.containsKey(id)) return epics.get(id);
        if (subTasks.containsKey(id)) return subTasks.get(id);
        return null;
    }

    // Переопределения с сохранением

    @Override
    public Task addTask(Task task) {
        Task result = super.addTask(task);
        save();
        return result;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic result = super.addEpic(epic);
        save();
        return result;
    }

    @Override
    public SubTask addSubTask(SubTask subTask) {
        SubTask result = super.addSubTask(subTask);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubTasks() {
        super.deleteSubTasks();
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int epicId) {
        super.removeEpic(epicId);
        save();
    }

    @Override
    public void removeSubTask(int id) {
        super.removeSubTask(id);
        save();
    }
}
