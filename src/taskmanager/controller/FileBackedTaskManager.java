package taskmanager.controller;

import taskmanager.model.Epic;
import taskmanager.model.SubTask;
import taskmanager.model.Task;
import taskmanager.util.Status;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description\n");
            //for (Task task : getAllTasks()) {
                writer.write("\n");
            //}
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении задач в файл: " + e.getMessage());
        }
    }
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                Task task = Task.fromCsv(line);
                manager.addTaskDirectly(task);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке задач из файла: " + e.getMessage());
        }

        return manager;
    }

    // Переопределяем методы родителя, добавляя save()

//    @Override
//    public ArrayList<Task> getTasks() {
//        return super.getTasks();
//        save();
//    }
//
//    @Override
//    public ArrayList<SubTask> getSubTasks() {
//        return super.getSubTasks();
//        save();
//    }
//
//    @Override
//    public ArrayList<Epic> getEpics() {
//        return super.getEpics();
//        save();
//    }
//
//    @Override
//    public Task getTask(int idTask) {
//        return super.getTask(idTask);
//        save();
//    }
//
//    @Override
//    public Epic getEpic(int idEpic) {
//        return super.getEpic(idEpic);
//        save();
//    }
//
//    @Override
//    public SubTask getSubTask(int idSubTask) {
//        return super.getSubTask(idSubTask);
//        save();
//    }
//
//    @Override
//    public ArrayList<SubTask> getSubTaskList(int epicId) {
//        return super.getSubTaskList(epicId);
//        save();
//    }

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
    public void getHistory() {
        super.getHistory();
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

//    @Override
//    public Status updateEpicStatus(int epicId) {
//        return super.updateEpicStatus(epicId);
//        save();
//    }
}
