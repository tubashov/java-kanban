import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int count;

    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, SubTask> subTasks;

//    public TaskManager() {
//        count = 0;
//    }
//    void increment(){
//        count++;
//    }
    public int generateId() {
        count++;
        return count;
    }
    private void deleteSubTask(){}

    public void addTask(Task task) {
        Task newTask = new Task(generateId(), task.getName(), task.getDescription(), task.getStatus());
        this.tasks.put(newTask.getId(), newTask);
    }
    public void addTask(Epic epic) {
        Epic newEpic = new Epic(generateId(), epic.getName(), epic.getDescription(), epic.getStatus(),
                epic.getIdSubTask());
        this.epics.put(newEpic.getId(), newEpic);
    }
    public void addTask(SubTask subTask) {
        SubTask newSubTask = new SubTask(generateId(), subTask.getName(), subTask.getDescription(), subTask.getStatus(),
                subTask.getEpicId());
        this.subTasks.put(newSubTask.getId(), newSubTask);
    }

    // при выполнении функции всех эпиков должны удаляться все подзадачи, если удаляется один эпик - удаляются его подзадачи
    private void changeEpicStatus(){
        String epicStatus = String.valueOf(Status.NEW);
        if (epics.get(epics.keySet().)
        // если подзадач нет, и ArrayList пустой epicStatus = NEW. Если все статусы NEW, epicStatus = NEW.
        // Если все статусы DOWN, epicStatus = DOWN, else epicStatus = IN_PROGRESS.
    }

}
