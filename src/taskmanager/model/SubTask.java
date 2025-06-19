package taskmanager.model;

import java.util.Objects;

import taskmanager.util.Status;
import taskmanager.util.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {

    private int epicId;

    public SubTask(int i, String string, String s, LocalDateTime localDateTime, Duration duration, Status aNew) {
    }

//    public SubTask(Integer id, String name, String description, Status status, LocalDateTime startTime,
//                   Duration duration, Status status) {
//        super(id, name, description, startTime, duration, status);
//    }

    public SubTask(Integer id, String name, String description, Status status, LocalDateTime startTime,
                   Duration duration, int epicId) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public SubTask(Integer id, String name, String description, Status status, LocalDateTime startTime,
                   Duration duration) {
        super(id, name, description, status, startTime, duration);
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
    public TaskType getType() {
        return TaskType.SUBTASK;
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
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                ", epicId=" + epicId +
                '}';
    }
}
