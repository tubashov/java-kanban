package taskmanager.controller;

import taskmanager.model.Task;

public class Node  {
    Task task;
    Node next;
    Node prev;

    public Node(Node prev, Task task, Node next) {
        this.task = task;
        this.next = null;
        this.prev = null;
    }
}
