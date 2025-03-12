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
    public void updateTask(int id, Task task) {
        Task newTask = new Task(id, task.getName(), task.getDescription(), task.getStatus());
        this.tasks.put(task.getId(), newTask);
    }
    public void updateEpic(int id, Epic epic) {
        Epic newEpic = new Epic(id, epic.getName(), epic.getDescription(), getEpicStatus(epic.getId()),
                epic.getIdSubTask());
        this.tasks.put(epic.getId(), newEpic);
    }
    public void updateTask(int id, SubTask subTask) {
        SubTask newSubTask = new SubTask(id, subTask.getName(), subTask.getDescription(), subTask.getStatus(),
                subTask.getEpicId());
        this.subTasks.put(subTask.getId(), subTask);
    }
    // получение всех задач
    public void getTasks() {
        for (Task task : tasks.values()) {
            System.out.println(task.toString());
        }
    }
    public void getEpics() {
        for (Epic epic : epics.values()) {
            System.out.println(epic.toString());
        }
    }
    public void getSubTasks() {
        for (SubTask subTask : subTasks.values()) {
            System.out.println(subTask.toString());
        }
    }
    // получение по идентификатору
    public Task getTask(int idTask) {
        for (Task task : tasks.values()) {
            if (task.getId() == idTask) {
                System.out.printf(task.toString());
            }
        }
        return  null;
    }
    public Epic getEpic(int idEpic) {
        for (Epic epic : epics.values()) {
            if (epic.getId() == idEpic) {
                System.out.println(epic.toString());
            }
        }
        return null;
    }
    public SubTask getSubTask(int idSubTask) {
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getId() == idSubTask) {
                System.out.println(subTask.toString());
            }
        }
        return null;
    }
    // список подзадач определенного эпика
    public void getSubTaskList(int epicId) {
        Epic epic = this.epics.get(epicId);
        List<Integer> subTaskIds = epic.getIdSubTask();
        for (Integer subTaskId : subTaskIds) {
            SubTask subTask =  subTasks.get(subTaskId);
            System.out.println(subTask.toString());
        }
    }
    // удаление всех задач
    public void crearTasks() {
        tasks.clear();
        }
    public void crearEpics() {
        epics.clear();
    }
    public void crearSubTasks() {
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
        subTasks.remove(id);
    }
    // изменение статуса эпика
    private Status getEpicStatus(int epicId){
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
