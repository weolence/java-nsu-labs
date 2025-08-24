package out_formatter;

/**
 * {@code OutFormatter} is a simple class for only output formatting.
 * It helps with printing exceptions from whole program and hoards data
 * for printing it in console by flush()
 * @see #flush
 * <p>
 *     The main {@code OutFormatter} task is cover needs of Forth class,
 *     so there are no extra functions
 * </p>
 */
public class OutFormatter {
    private static String buffer = "";

    public OutFormatter() { }

    /**
     * Appends value to main string buffer
     * @param string Value which will be added to buffer
     */
    public void append(String string) {
        buffer = buffer + string + " ";
    }

    /**
     * Reset of static buffer
     */
    public void clear() {
        buffer = "";
    }

    /**
     * Allow to check emptiness of string buffer
     * @return true if buffer empty, false if there is at least one symbol in string buffer
     */
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    /**
     * Flushes into console whole buffer and then
     * clears it for future using
     */
    public void flush() {
        if(buffer.isEmpty()) {
            return;
        }
        System.out.println(buffer);
        clear();
    }

    /**
     * Prints text of exceptions
     * @param exception Exception which will be printed
     */
    public void printError(Exception exception) {
        System.out.println("error: " + exception.getMessage());
    }

    /**
     * Prints special symbol '>' which means readiness for
     * taking input from Console/File
     */
    public void printReady() {
        System.out.print("> ");
    }

    /**
     * Prints special symbol '<' which means correct interpretation
     */
    public void printAnswer() {
        System.out.print("< ");
    }

    /**
     * Prints special symbol 'ok' which means receiving
     * a line from input
     */
    public void printAcceptance() {
        System.out.println("ok");
    }
}
