import java.util.HashMap;

public class Main {
    public static void main(String[] args) {


        TaskManager taskManager = new TaskManager();

        Task task = new Task(taskManager.generateId(), "Забронировать отель", "Вид на море, бассейн на территории", Status.NEW);
        Task task1 = new Task(taskManager.generateId(), "Купить билеты на самолет", "Утренний рейс, туда-обратно", Status.NEW);

        taskManager.addTask(task);
        taskManager.addTask(task1);

        SubTask subTask = new SubTask(taskManager.generateId(), "Купальные принадлежности", "Трусы в горошек", Status.NEW);
        SubTask subTask1 = new SubTask(taskManager.generateId(), "Косметика", "Крем от загара", Status.NEW);
        SubTask subTask2 = new SubTask(taskManager.generateId(), "Сделать уборку", "Полить цветы", Status.NEW);
        taskManager.addTask(subTask);
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);

        Epic epic = new Epic(taskManager.generateId(), "Дела до отъезда", "Собрать вещи", Status.NEW);
        Epic epic1 = new Epic(taskManager.generateId(), "Дела до отъезда", "Домашние дела", Status.NEW);
        epic.setIdSubTask(subTask.getId());
        epic.setIdSubTask(subTask1.getId());
        epic1.setIdSubTask(subTask.getId());

        taskManager.addEpic(epic);
        taskManager.addEpic(epic1);
        subTask.setEpicId(epic.getId());
        subTask1.setEpicId(epic.getId());
        subTask2.setEpicId(epic1.getId());


        System.out.println();
        System.out.println("Получение списка всех задач:");
        taskManager.getTasks();
        taskManager.getSubTasks();
        taskManager.getEpics();

        System.out.println();
        System.out.println("Получение объекта по идентификатору:");
        taskManager.getEpic(epic1.getId());

        System.out.println();
        System.out.println("Получение всех подзадач epic:");
        taskManager.getSubTaskList(epic.getId());


        System.out.println();
        System.out.println("Новая версия объекта:");
        subTask = new SubTask(taskManager.generateId() , "Купальные принадлежности","Трусы в цветочек", Status.DOWN, epic.getId());
        taskManager.updateSubTask(subTask);
        epic.setIdSubTask(subTask.getId());
        taskManager.getSubTaskList(epic.getId());
        System.out.println();
        System.out.println("Обновление статуса epic:");
        Status status = taskManager.getEpicStatus(epic.getId());
        epic.setStatus(status);
        taskManager.getEpic(epic.getId());

        System.out.println();
        System.out.println("Удаление подзадачи:");
        taskManager.removeSubTask(subTask1.getId());
        taskManager.getSubTasks();

        System.out.println();
        System.out.println("Удаление всех задач:");
        taskManager.crearTasks();
        taskManager.crearSubTasks();
        taskManager.crearEpics();
        taskManager.getTasks();
        taskManager.getEpics();
        taskManager.getSubTasks();
    }
}
