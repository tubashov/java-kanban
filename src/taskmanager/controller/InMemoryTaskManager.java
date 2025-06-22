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

        boolean hasTime = task.getStartTime() != null && task.getDuration() != null;

        // есть время, и задача пересекается — исключение
        if (hasTime && hasIntersection(task)) {
            throw new IllegalArgumentException("Ошибка: задача пересекается по времени.");
        }

        // есть время и нет пересечения — добавить в отсортированный список
        if (hasTime) {
            tryAddToPrioritized(task);
        }

        // добавить в основной список
        tasks.put(id, task);
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

        boolean hasTime = subTask.getStartTime() != null && subTask.getDuration() != null;

        // есть время, нет пересечений с другими задачами
        if (hasTime && hasIntersection(subTask)) {
            throw new IllegalArgumentException("Ошибка: подзадача пересекается по времени с другой задачей.");
        }

        // добавить в приоритетный список
        if (hasTime) {
            tryAddToPrioritized(subTask);
        }

        // добавить общий список
        subTasks.put(id, subTask);


        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.getIdSubTask().add(id); // добавить id подзадачи в эпик
            updateEpicStatus(epicId);   // обновлить статус эпика
            updateEpicTime(epic);       // обновить время эпика
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
        return epics.get(epicId).getIdSubTask().stream()
                .map(subTasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // обновление задачи
    @Override
    public void updateTask(Task task) {
        boolean hasTime = task.getStartTime() != null && task.getDuration() != null;

        tryRemoveFromPrioritized(task); // удаление старой версии перед обновлением

        // задача не пересекается по врмемени с другими — в оба списка
        if (hasTime) {
            if (hasIntersection(task)) { // пересекается по врмемени
                throw new IllegalArgumentException();
            }
            tryAddToPrioritized(task); // отсортированный список
        }

        // не пересекается по времени или нет времени - основной список
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        tasks.put(epic.getId(), epic);
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        boolean hasTime = subTask.getStartTime() != null && subTask.getDuration() != null;

        tryRemoveFromPrioritized(subTasks.get(subTask.getId())); // удаление старой версии перед обновлением

        // подзадача не пересекается по врмемени с другими — в оба списка
        if (hasTime) {
            if (hasIntersection(subTask)) { // пересекается по врмемени
                throw new IllegalArgumentException();
            }
            tryAddToPrioritized(subTask);
        }

        // обновление подзадачи
        subTasks.put(subTask.getId(), subTask);

        // обновление статуса и времени эпика
        Integer epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.setStatus(updateEpicStatus(epicId));
            updateEpicTime(epic);
        }
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
            tryRemoveFromPrioritized(tasks.get(taskId));
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Integer taskId : epics.keySet()) {
            historyManager.remove(taskId);
            tryRemoveFromPrioritized(tasks.get(taskId));
        }
        tasks.keySet().forEach(historyManager::remove);
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteSubTasks() {
        for (SubTask subTask : subTasks.values()) {
            tryRemoveFromPrioritized(subTask);
            historyManager.remove(subTask.getId());
        }
        for (Epic epic : epics.values()) {
            epic.getIdSubTask().clear();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic);
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
        List<Integer> idSubTasks = epic.getIdSubTask();
        for (Integer idSubTask : idSubTasks) {
            subTasks.remove(idSubTask);
            historyManager.remove(idSubTask);
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
        Epic epic = this.epics.get(epicId);
        if (epic == null) return null;

        List<SubTask> subTasksOfEpic = epic.getIdSubTask().stream()
                .map(subTasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long newCount = subTasksOfEpic.stream()
                .filter(sub -> sub.getStatus() == Status.NEW)
                .count();

        long doneCount = subTasksOfEpic.stream()
                .filter(sub -> sub.getStatus() == Status.DONE)
                .count();

        int total = subTasksOfEpic.size();

        Status newStatus;
        if (total == 0 || newCount == total) {
            newStatus = Status.NEW;
        } else if (doneCount == total) {
            newStatus = Status.DONE;
        } else {
            newStatus = Status.IN_PROGRESS;
        }

        epic.setStatus(newStatus); // установка статуса
        return newStatus;
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

        // переменные для вычислений
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
        epic.setEndTime(end);
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

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // проверка пересечения новой задачи с остальными
    private boolean hasIntersection(Task newTask) {
        return prioritizedTasks.stream()
                .filter(t -> !t.getId().equals(newTask.getId())) // проверка с собой
                .anyMatch(t -> isOverlapping(t, newTask));
    }

}
