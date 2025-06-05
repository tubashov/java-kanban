package taskmanager.controller;

import taskmanager.model.*;
import taskmanager.util.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private final Charset charset;

    public FileBackedTaskManager(File file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    // сохранение задач в файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)
                , StandardCharsets.UTF_8))) {
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
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении задач в файл", e);
        }
    }

    // сохранения задачи в строку
    public static String taskToString(Task task) {
        String type;
        String epicId = "";

        if (task instanceof Epic) {
            type = "EPIC";
        } else if (task instanceof SubTask) {
            type = "SUBTASK";
            epicId = String.valueOf(((SubTask) task).getEpicId());
        } else {
            type = "TASK";
        }
        return task.getId() + "," +
                type + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                epicId;
    }

    // метод создания задачи из строки
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
    public static  FileBackedTaskManager loadFromFile (File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file, StandardCharsets.UTF_8);

        Map<Integer, Task> tasks = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                Task task = fromString(line);
                if (task instanceof Epic) {
                    manager.addTask((Epic) task);
                } else if (task instanceof SubTask) {
                    manager.addTask((SubTask) task);
                } else {
                    manager.addTask(task);
                }
            }
        } catch(IOException e){
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
        return manager;
    }

    // восстановление менеджера из файла
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
    public List<Task> getHistory() {
        List<Task> history = super.getHistory();
        save();
        return history;
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
