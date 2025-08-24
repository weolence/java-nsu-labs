package exceptions;

public class SyntaxException extends RuntimeException {
    public SyntaxException(String info) {
        super(info);
    }
}
