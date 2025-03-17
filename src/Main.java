import com.sun.org.apache.bcel.internal.generic.NEW;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.SubTask;
import tracker.model.Task;
import tracker.util.Status;

public class Main {
    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();
//        Task task = new Task();
        Epic epic = new Epic();
        SubTask subTask = new SubTask();

        Task task = new Task(taskManager.generateId(), "Забронировать отель", "Вид на море, бассейн на территории", Status.NEW);
        Task task1 = new Task(taskManager.generateId(), "Купить билеты на самолет", "Утренний рейс, туда-обратно", Status.NEW);
        taskManager.addTask(task);
        taskManager.addTask(task1);

        subTask = new SubTask(taskManager.generateId(), "Купальные принадлежности", "Трусы в горошек", Status.NEW);
        SubTask subTask1 = new SubTask(taskManager.generateId(), "Косметика", "Крем от загара", Status.NEW);
        SubTask subTask2 = new SubTask(taskManager.generateId(), "Сделать уборку", "Полить цветы", Status.NEW);
        taskManager.addTask(subTask);
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);

        epic = new Epic(taskManager.generateId(), "Дела до отъезда", "Собрать вещи");
        Epic epic1 = new Epic(taskManager.generateId(), "Дела до отъезда", "Домашние дела");
        taskManager.addEpic(epic);
        taskManager.addEpic(epic1);
        epic.setIdSubTask(subTask.getId());
        epic.setIdSubTask(subTask1.getId());
        epic1.setIdSubTask(subTask2.getId());

        subTask.setEpicId(epic.getId());
        subTask1.setEpicId(epic.getId());
        subTask2.setEpicId(epic1.getId());

        System.out.println();
        System.out.println("Получение списка всех задач:");
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpics());

        System.out.println();
        System.out.println("Получение объекта по идентификатору:");
        task = taskManager.getTask(task.getId());
        System.out.println(task);

        System.out.println();
        System.out.println("Получение всех подзадач epic:");
        System.out.println(taskManager.getSubTaskList(epic.getId()));


        System.out.println();
        System.out.println("Новая версия объекта:");
        subTask = new SubTask(subTask.getId() , "Купальные принадлежности","Трусы в цветочек", Status.DOWN, epic.getId());
        taskManager.updateSubTask(subTask);
        String newVersion = String.valueOf(taskManager.getSubTask(subTask.getId()));
        System.out.println(newVersion);

        System.out.println();
        System.out.println("Обновление статуса epic:");
        System.out.println(taskManager.getSubTaskList(epic.getId()));
        System.out.println(taskManager.getEpic(epic.getId()));

        System.out.println();
        System.out.println("Удаление подзадачи:");
        taskManager.removeSubTask(subTask1.getId());
        System.out.println(taskManager.getSubTaskList(epic.getId()));
        System.out.println(taskManager.getSubTaskList(epic1.getId()));
        System.out.println(taskManager.getEpic(epic.getId()));

        System.out.println();
        System.out.println("Удаление всех задач:");
        taskManager.deleteTasks();
        taskManager.deleteSubTasks();
        taskManager.deleteEpics();
        taskManager.getTasks();
        taskManager.getEpics();
        taskManager.getSubTasks();
    }
}
