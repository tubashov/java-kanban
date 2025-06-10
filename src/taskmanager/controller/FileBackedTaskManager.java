package taskmanager.controller;

import taskmanager.exceptions.ManagerSaveException;
import taskmanager.model.*;
import taskmanager.util.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private final Charset charset;

    public FileBackedTaskManager(File file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    // сохранение задач в файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8))) {
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
            writer.newLine(); // разделяем блок задач и историю
            writer.write(historyToString(getHistoryManager()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении задач в файл", e);
        }
    }

    // сохранения задачи в строку
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

    // метод получения задачи из строки
    public static Task taskFromString(String value) {
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
                int epicId = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
                return new SubTask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    // загрузка данных из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            // Пропускаем заголовок
            String line = reader.readLine();

            // Читаем задачи, пока не встретим пустую строку или конец файла
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break; // достигли конца задач, дальше история
                }
                try {
                    Task task = taskFromString(line);
                    switch (task.getType()) {
                        case EPIC:
                            manager.addEpic((Epic) task);
                            break;
                        case SUBTASK:
                            manager.addSubTask((SubTask) task);
                            break;
                        case TASK:
                        default:
                            manager.addTask(task);
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при разборе задачи из строки: '" + line + "'. Пропущена.");
                }
            }

            // Читаем строку истории (может быть null или пустой)
            String historyLine = reader.readLine();
            while (historyLine != null && historyLine.trim().isEmpty()) {
                historyLine = reader.readLine(); // пропускаем пустые строки
            }

            if (historyLine != null && !historyLine.trim().isEmpty()) {
                List<Integer> historyIds = historyFromString(historyLine);
                for (int id : historyIds) {
                    Task task = manager.getTaskById(id);
                    if (task != null) {
                        manager.getHistoryManager().add(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
        return manager;
    }


    // получение истории из строки
    public static List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return history;
        }

        String[] ids = value.split(",");
        for (String idStr : ids) {
            String trimmed = idStr.trim();
            try {
                history.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException e) {
                // Логируем или игнорируем некорректные значения
                System.err.println("Пропущен некорректный id в истории: '" + trimmed + "'");
            }
        }
        return history;
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

    // поиск задачи по id
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        }
        if (epics.containsKey(id)) {
            return epics.get(id);
        }
        if (subTasks.containsKey(id)) {
            return subTasks.get(id);
        }
        return null;
    }

    // переопределения
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
