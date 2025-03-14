import java.util.HashMap;

public class Main {
    public static void main(String[] args) {


        TaskManager taskManager = new TaskManager();

        Task task = new Task( 1,"Забронировать отель", "Вид на море, бассейн на территории", Status.NEW);
        Task task1 = new Task(2, "Купить билеты на самолет", "Утренний рейс, туда-обратно", Status.NEW);
        Task task2 = new Task(3, "Купить билеты на поезд", "СВ", Status.NEW);
        taskManager.addTask(task);
        taskManager.addTask(task1);

        SubTask subTask = new SubTask(6 , "Купальные принадлежности", "Трусы в горошек", Status.NEW);
        SubTask subTask1 = new SubTask(7, "Косметика", "Крем от загара", Status.NEW);
        SubTask subTask2 = new SubTask(8, "Сделать убоку", "Полить цветы", Status.NEW);
        taskManager.addTask(subTask);
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);

        Epic epic = new Epic(4, "Дела до отъезда", "Собрать вещи", Status.NEW);
        Epic epic1 = new Epic(5, "Дела до отъезда", "Домашние дела", Status.NEW);
        epic.setIdSubTask(6);
        epic.setIdSubTask(7);
        epic1.setIdSubTask(8);
        taskManager.addEpic(epic);
        taskManager.addEpic(epic1);
        subTask.setEpicId(4);
        subTask1.setEpicId(4);
        subTask2.setEpicId(5);

        System.out.println("Получение списка всех задач:");
        taskManager.getTasks();
        taskManager.getSubTasks();
        taskManager.getEpics();

        System.out.println("получение по идентификатору:");
        taskManager.getEpic(6);
//        taskManager.updateTask(task1.getId(), task2);
//        taskManager.getTasks();



//      Измените статусы созданных объектов, распечатайте их. Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.


//      И, наконец, попробуйте удалить одну из задач и один из эпиков.
//        int epicId = epic.getId();
//        taskManager.removeEpic(epicId);
//        System.out.println(taskManager.getEpics());
//        System.out.println(taskManager.getSubTasks());
//        taskManager.crearTasks();
//        taskManager.crearSubTasks();
//        taskManager.crearEpics();
//        taskManager.getTasks();
//        taskManager.getEpics();
//        taskManager.getSubTasks();
    }
}
