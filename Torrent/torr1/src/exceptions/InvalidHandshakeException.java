package exceptions;

public class InvalidHandshakeException extends Exception {
    public InvalidHandshakeException() {
        super("invalid handshake data");
    }
}
