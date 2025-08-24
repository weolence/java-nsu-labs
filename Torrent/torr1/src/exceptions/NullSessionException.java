package exceptions;

public class NullSessionException extends Exception {
    public NullSessionException() {
        super("non-existing session received");
    }
}