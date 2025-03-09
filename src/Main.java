import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {


        TaskManager taskManager = new TaskManager();

        Task task = new Task(taskManager.generateId(), "Забронировать отель", "Вид на море, бассейн на территории", Status.NEW);
        Task task1 = new Task(taskManager.generateId(), "Купить билеты", "Утренний рейс, туда-обратно", Status.NEW);


        Epic epic = new Epic(taskManager.generateId(), "Собрать вещи", "Берём большой чемодан", Status.NEW, ArrayList<Integer> idSubTask);
        Epic epic1 = new Epic(taskManager.generateId(), "Собрать вещи", "Берём большой чемодан", Status.NEW, ArrayList<Integer> idSubTask);
        SubTask subTask = new SubTask(taskManager.generateId(), "Купальные принадлежности", "Трусы в горошек", Status.NEW, epic.getId());
        SubTask subTask1 = new SubTask(taskManager.generateId(), "Косметика", "Крем от загара 50+", Status.NEW, epic.getId());

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubTasks());
//      Измените статусы созданных объектов, распечатайте их. Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.

        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubTasks());
//      И, наконец, попробуйте удалить одну из задач и один из эпиков.
        int epicId = epic.getId();
        taskManager.removeEpic(epicId);
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubTasks());
    }
}
