package tracker.model;

import tracker.util.Status;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {

    private ArrayList<Integer> idSubTasks = new ArrayList<>();

    public Epic() {
    }

    public Epic(Integer id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Epic(Integer id, String name, String description, Status status, ArrayList<Integer> idSubTasks) {
        super(id, name, description, status);
        this.idSubTasks = idSubTasks;
    }

    public Epic(ArrayList<Integer> idSubTasks) {
        this.idSubTasks = idSubTasks;
    }

    public void setIdSubTask(int idSubTask) {
        this.idSubTasks.add(idSubTask);
    }

    public void cleanSubtaskIds() {
        idSubTasks.clear();
    }
    @Override
    public void setStatus(Status status) {
        super.setStatus(status);
    }

    public ArrayList<Integer> getIdSubTask() {
        return idSubTasks;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Epic epic = (Epic) object;
        return Objects.equals(idSubTasks, epic.idSubTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idSubTasks);
    }

    @Override
    public String toString() {
        return "tracker.controllers.model.Epic{id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() + ", " +
                "idSubTask=" + getIdSubTask() + "}";
    }
}