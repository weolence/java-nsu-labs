package exceptions;

public class InvalidPieceDataException extends Exception {
    public InvalidPieceDataException() {
        super("invalid piece data");
    }
}
