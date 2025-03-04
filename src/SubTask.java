public class SubTask extends Task {
    public SubTask(int idNumber, String name, String description, String status) {
        super(idNumber, name, description, Status.valueOf(status));
    }
}
