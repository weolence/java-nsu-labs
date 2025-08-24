package exceptions;

public class IllegalMessageTypeException extends Exception {
    public IllegalMessageTypeException() {
        super("illegal type of message");
    }
}