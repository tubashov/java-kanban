package tracker.model;

import java.util.Objects;
import tracker.util.Status;

public class SubTask extends Task {

    private int epicId;

    public SubTask() {
    }

    public SubTask(Integer id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public SubTask(Integer id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public SubTask(Integer epicId) {
        this.epicId = epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        SubTask subTask = (SubTask) object;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }
    
    @Override
    public String toString() {
        return "tracker.controllers.model.SubTask{id=" + getId() +
        ", name='" + getName() + '\'' +
        ", description='" + getDescription() + '\'' +
        ", status=" + getStatus() + ", " +
        "epicId=" + getEpicId() + "}";
    }
}
