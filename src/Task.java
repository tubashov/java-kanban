import java.util.ArrayList;
import java.util.HashMap;
public class Task {
    private final int idNumber;
    private final String name;
    private final String description;
    private final Status status;


    public Task(int idNumber, String name, String description, Status status) {
        this.idNumber = idNumber;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getIdNumber() {
        return idNumber;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {

        return status;
    }
}
