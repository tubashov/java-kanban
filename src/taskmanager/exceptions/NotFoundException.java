package taskmanager.exceptions;

public class NotFoundException extends Exception {
    public NotFoundException(String message) { // исключение, если объект не найден
        super(message);
    }
}
