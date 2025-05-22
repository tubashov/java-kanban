package taskmanager.controllers;

import taskmanager.model.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    public InMemoryHistoryManager() {

    }

    private Map<Integer, Node> history = new LinkedHashMap<>();

    private Node head;
    private Node tail;
    //private List<Task> history = new ArrayList<>();

    // просмотр истории
    @Override
    public void add(Task task) { // метод добавления просмотренной задачи, удаление предыдущих дублей
        if (task == null) {
            return;
        }
        if (history.containsKey(task.getId())) {
            remove(task.getId());
        }
        Node newNode = linkLast(task);
        history.put(task.getId(), newNode);
    }

    private Node linkLast(Task task) { // добавление новой просмотренной задачи в конец списка
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        tail = newNode;
        return newNode;
    }

    @Override
    public List<Task> getHistory() { // метод сбора задач из двусвязанного списка в ArrayList
        List<Task> taskHistory = new ArrayList<>();
        Node tasks = head;
        while (tasks != null) {
            taskHistory.add(tasks.task);
            tasks = tasks.next;
        }
        return taskHistory;
    }

        private void removeNode(Node node) { // метода удаления задачи из двухсвязанного списка
        if (node == null) {
            return;
        }
        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
            } else {
            head = next;
        }
        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
    }

    @Override
    public void remove(int id) { // метод удаления задачи из HashMap по id
        Node node = history.remove(id);
        removeNode(node);
    }
}
