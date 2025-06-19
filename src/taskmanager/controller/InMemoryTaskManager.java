package taskmanager.controller;

import taskmanager.model.Epic;
import taskmanager.model.SubTask;
import taskmanager.model.Task;
import taskmanager.util.Status;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int count;

    private final HistoryManager historyManager = new InMemoryHistoryManager();

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator
                    .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    // коллекция для хранения всех типов
    final HashMap<Integer, Task> tasks = new HashMap<>();
    final HashMap<Integer, Epic> epics = new HashMap<>();
    final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    @Override
    public int generateId() {
        count++;
        return count;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public Task addTask(Task task) {
        int id = generateId();
        task.setId(id);
        this.tasks.put(id, task);
        tryAddToPrioritized(task);
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        this.epics.put(id, epic);
        return epic;
    }

    @Override
    public SubTask addSubTask(SubTask subTask) {
        int id = generateId();
        subTask.setId(id);
        this.subTasks.put(id, subTask);
        tryAddToPrioritized(subTask);
        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.setIdSubTask(id); // также нужно добавить id подзадачи в список эпика
            epic.setStatus(updateEpicStatus(epicId)); // обновление статуса
            updateEpicTime(epic);
        }
        return subTask;
    }

    // получение списка всех задач
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // получение по идентификатору
    @Override
    public Task getTask(int idTask) {
        final Task task = tasks.get(idTask);
        if (task == null) {
            return null;
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int idEpic) {
        final Epic epic = epics.get(idEpic);
        if (epic == null) {
            return null;
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTask(int idSubTask) {
        final SubTask SubTask = subTasks.get(idSubTask);
        if (SubTask == null) {
            return null;
        }
        historyManager.add(SubTask);
        return SubTask;
    }

    // список подзадач определенного эпика
    @Override
    public ArrayList<SubTask> getSubTaskList(int epicId) {
        ArrayList<SubTask> listSubTasks = new ArrayList<>();
        Epic epic = this.epics.get(epicId);
        List<Integer> SubTaskIds = epic.getIdSubTask();
        for (Integer SubTaskId : SubTaskIds) {
            SubTask SubTask = subTasks.get(SubTaskId);
            listSubTasks.add(SubTask);
        }
        return listSubTasks;
    }

    // обновление задачи
    @Override
    public void updateTask(Task task) {
        tryRemoveFromPrioritized(task);
        tasks.put(task.getId(), task);
        tryAddToPrioritized(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        tasks.put(epic.getId(), epic);
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        tryRemoveFromPrioritized(subTask);
        this.subTasks.put(subTask.getId(), subTask);
        tryAddToPrioritized(subTask);
        Integer idEpic = subTask.getEpicId();
        Epic epic = epics.get(idEpic);
        Status status = updateEpicStatus(idEpic);
        epic.setStatus(status);
        updateEpicTime(epic);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // удаление всех задач
    @Override
    public void deleteTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Integer taskId : epics.keySet()) {
            historyManager.remove(taskId);
        }
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteSubTasks() {
        for (Epic epic : epics.values()) {
            epic.getIdSubTask().clear();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic);
        }
        for (SubTask subTask : subTasks.values()) {
            tryRemoveFromPrioritized(subTask);
            historyManager.remove(subTask.getId());
        }
        subTasks.clear();
    }

    // удаление по идентификатору
    @Override

    public void removeTask(int id) {
            tryRemoveFromPrioritized(tasks.get(id));
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int epicId) {
        Epic epic = this.epics.get(epicId);
        List<Integer> SubTaskIds = epic.getIdSubTask();
        for (Integer SubTaskId : SubTaskIds) {
            subTasks.remove(SubTaskId);
            historyManager.remove(SubTaskId);
        }
        epics.remove(epicId);
        historyManager.remove(epicId);
    }

    @Override
    public void removeSubTask(int id) {
        Integer epicId = subTasks.get(id).getEpicId();
        tryRemoveFromPrioritized(subTasks.get(id));
        subTasks.remove(id);
        Epic epic = epics.get(epicId);
        epic.setIdSubTask(id);
        Status status = updateEpicStatus(epicId);
        epic.setStatus(status);
        updateEpicTime(epic);
        historyManager.remove(id);
    }

    // изменение статуса эпика
    @Override
    public Status updateEpicStatus(int epicId) {
        int newCount = 0;
        int doneCount = 0;
        Epic epic = this.epics.get(epicId); // создаем объект класса epic с epicId, поступившего в параметре метода
        List<Integer> SubTaskIds = epic.getIdSubTask(); // создаем список idSubTask из этого epica
        for (Integer SubTaskId : SubTaskIds) { // обход всех элементов списка
            SubTask SubTask = this.subTasks.get(SubTaskId); // перебор всех подзадач из хэш-таблицы SubTasks
            if (SubTask.getStatus() == Status.NEW) { // если статус подзадачи в хэш -таблице совпадает со статусом
                newCount++;                          // подзадачи рассматриваемог epica, срабатывает счетчик
            } else if (SubTask.getStatus() == Status.DONE) {
                doneCount++;
            }
        }
        if (SubTaskIds.size() == newCount) { // определяем по счетчикам статус epica
            return Status.NEW;
        } else if (SubTaskIds.size() == doneCount) {
            return Status.DONE;
        } else {
            return Status.IN_PROGRESS;
        }
    }

    // расчет времени эпика
    private void updateEpicTime(Epic epic) {
        List<SubTask> epicSubTasks = epic.getIdSubTask().stream()
                .map(subTasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (epicSubTasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        Duration totalDuration = Duration.ZERO;

        for (SubTask sub : epicSubTasks) {
            LocalDateTime subStart = sub.getStartTime();
            LocalDateTime subEnd = sub.getEndTime();

            if (subStart != null) {
                if (start == null || subStart.isBefore(start)) {
                    start = subStart;
                }
            }

            if (subEnd != null) {
                if (end == null || subEnd.isAfter(end)) {
                    end = subEnd;
                }
            }

            if (sub.getDuration() != null) {
                totalDuration = totalDuration.plus(sub.getDuration());
            }
        }

        epic.setStartTime(start);
        epic.setDuration(totalDuration);
    }

    // проверка при добавлении задачи в отсортированный список
    private void tryAddToPrioritized(Task task) {
        if (task != null && task.getStartTime() != null) {
            if (hasIntersection(task)) {
                throw new IllegalArgumentException("Ошибка: задача пересекается по времени с другой.");
            }
            prioritizedTasks.add(task);
        }
    }

    // проверка при удалении задачи из отсортированного списка
    private void tryRemoveFromPrioritized(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    // проверка пересечения двух задач
    private boolean isOverlapping(Task t1, Task t2) {
        if (t1.getStartTime() == null || t1.getDuration() == null ||
                t2.getStartTime() == null || t2.getDuration() == null) {
            return false;
        }

        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = start1.plus(t1.getDuration());
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = start2.plus(t2.getDuration());

        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    // проверка пересечения новой задачи с остальными
    private boolean hasIntersection(Task newTask) {
        return prioritizedTasks.stream()
                .filter(t -> !t.getId().equals(newTask.getId())) // проверка с собой
                .anyMatch(t -> isOverlapping(t, newTask));
    }

}
