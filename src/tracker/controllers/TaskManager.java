package tracker.controllers;

import tracker.model.Epic;
import tracker.model.SubTask;
import tracker.model.Task;
import tracker.util.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int count;
    // коллекция для хранения всех типов
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();


    public int generateId() {
        count++;
        return count;
    }

    public Task addTask(Task task) {
        int id = generateId();
        task.setId(id);
        this.tasks.put(id, task);
        return task;
    }
    public Epic addEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        this.epics.put(id, epic);
        return epic;
    }
    public SubTask addTask(SubTask subTask) {
        int id = generateId();
        subTask.setId(id);
        this.subTasks.put(id, subTask);
        return subTask;
    }
    // обновление задачи
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        tasks.put(epic.getId(), epic);
    }
    public void updateSubTask(SubTask subTask) {
        this.subTasks.put(subTask.getId(), subTask);
        Integer id = subTask.getEpicId();
        Status status = updateEpicStatus(id);
        Epic epic = epics.get(id);
        epic.setStatus(status);
    }
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }
    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // получение по идентификатору
    public Task getTask(int idTask) {
        for (Task task : tasks.values()) {
            if (task.getId() == idTask) {
               return task;
            }
        }
        return  null;
    }
    public Epic getEpic(int idEpic) {
        for (Epic epic : epics.values()) {
            if (epic.getId() == idEpic) {
                return epic;
            }
        }
        return null;
    }
    public SubTask getSubTask(int idSubTask) {
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getId() == idSubTask) {
                return subTask;
            }
        }
        return null;
    }
    // список подзадач определенного эпика
    public ArrayList<SubTask> getSubTaskList(int epicId) {
        ArrayList<SubTask> listSubTasks = new ArrayList<>();
        Epic epic = this.epics.get(epicId);
        List<Integer> subTaskIds = epic.getIdSubTask();
        for (Integer subTaskId : subTaskIds) {
            SubTask subTask =  subTasks.get(subTaskId);
            listSubTasks.add(subTask);
        }
        return listSubTasks;
    }
    // удаление всех задач
    public void deleteTasks() {
        tasks.clear();
    }
    public void deleteEpics() {
        epics.clear();
        subTasks.clear();
    }

    public void deleteSubTasks() {
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            updateEpicStatus(epic.getId());
        }
        subTasks.clear();
    }
    // удаление по идентификатору
    public void removeTask(int id ) {
        tasks.remove(id);
    }
    public void removeEpic(int epicId) {
        Epic epic = this.epics.get(epicId);
        List<Integer> subTaskIds =  epic.getIdSubTask();
        for (Integer subTaskId : subTaskIds) {
            subTasks.remove(subTaskId);
        }
        epics.remove(epicId);
    }
    public void removeSubTask(int id) {
        Integer epicId = subTasks.get(id).getEpicId();
        subTasks.remove(id);

        epic.cleanSubtaskIds();
        updateEpicStatus(epicId);

    }

    // изменение статуса эпика
    public Status updateEpicStatus(int epicId){
        int newCount = 0;
        int doneCount = 0;
        Epic epic = this.epics.get(epicId); // создаем объект класса epic с epicId, поступившего в параметре метода
        List<Integer> subTaskIds = epic.getIdSubTask(); // создаем список idSubtask из этого epica
        for(Integer subTaskId : subTaskIds) { // обход всех элементов списка
            SubTask subTask = this.subTasks.get(subTaskId); // перебор всех подзадач из хэш-таблицы subTasks
            if (subTask.getStatus() == Status.NEW) { // если статус подзадачи в хэш -таблице совпадает со статусом
                newCount++;                          // подзадачи рассматриваемог epica, срабатывает счетчик
            } else if (subTask.getStatus() == Status.DOWN) {
                doneCount++;
            }
        }
        if (subTaskIds.size() == newCount) { // определяем по счетчикам статус epica
            return Status.NEW;
        } else if (subTaskIds.size() == doneCount) {
            return Status.DOWN;
        } else {
            return Status.IN_PROGRESS;
        }
    }
}
