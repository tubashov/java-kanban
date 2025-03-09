import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {

    private ArrayList<Integer> idSubTask;

    public Epic(int id, String name, String description, Status status, ArrayList<Integer> idSubTask) {
        super(id, name, description, status);
        this.idSubTask = idSubTask;
    }

    public void setIdSubTask(ArrayList<Integer> idSubTask) {
        this.idSubTask = idSubTask;
    }

    public ArrayList<Integer> getIdSubTask() {
        return idSubTask;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Epic epic = (Epic) object;
        return Objects.equals(idSubTask, epic.idSubTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idSubTask);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "idSubTask=" + idSubTask +
                '}';
    }
}
