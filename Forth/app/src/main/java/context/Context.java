package context;

import data_types.MutableInt;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code Context} stores and manages few data structures.
 * It needed for passing required data to execute() commands.
 * So there are a few methods for changing state of data, without changing references to those structures.
 * <p>
 *     {@code Context} constructed as a simple container so all of its methods are primal
 *     and intuitively understandable
 * </p>
 */
public class Context {
    private final MutableInt programCounter;
    private final Map<String, ArrayList<String>> definedCommands;
    private final ArrayList<Integer> stack;
    private final ArrayList<String> line;

    private final InputStream in;
    private final ByteArrayOutputStream out;
    private final PrintStream outStream;

    /**
     * Once constructed {@code Context} won't be changeable in meaning of references,
     * because do it much safer(fields won't be barely changed). So {@code Context} must be reconstructed every time.
     * @param definedCommands Map filled by String-ArrayList, it helps find definition by name or add definition by name
     * @param programCounter PC from assembly instructions, it shows which token executes
     * @param stack Stack for integers
     * @param line Current line, parsed from input
     */
    public Context(Map<String, ArrayList<String>> definedCommands, MutableInt programCounter, ArrayList<Integer> stack, ArrayList<String> line) {
        this.definedCommands = definedCommands;
        this.programCounter = programCounter;
        this.stack = stack;
        this.line = line;

        String input = "";
        in = Context.class.getResourceAsStream(input);
        out = new ByteArrayOutputStream();
        outStream = new PrintStream(out);
    }

    // Input-Output section

    public InputStream inGet() {
        return in;
    }

    public PrintStream outStreamGet() {
        return outStream;
    }

    public String outFlush() {
        return out.toString();
    }
    // Defined Commands section

    /**
     * Gets defined command from map
     * @param key gets value from pair
     * @return value from map by that key
     */
    public ArrayList<String> definedCommandGet(String key) {
        return definedCommands.get(key);
    }

    /**
     * Allow to put define command in map(means create pair String-ArrayList)
     * @param key name of self-defined command
     * @param value definition of self-defined command
     */
    public void defineCommandPut(String key, ArrayList<String> value) {
        definedCommands.put(key, value);
    }

    // Program counter section

    /**
     * Method from get from AtomicInt
     * @return value of program counter
     */
    public int programCounterGet() {
        return programCounter.get();
    }

    /**
     * Allows to change value of program counter
     * @param value value which will be set in program counter
     */
    public void programCounterSet(int value) {
        programCounter.set(value);
    }

    // Stack section

    /**
     * Allows to get a value from stack by index
     * @param index index of value in stack
     * @return value by index
     */
    public int stackGet(int index) {
        return stack.get(index);
    }

    /**
     * Allows to set value in stack by index
     * @param index index of value in stack
     * @param value value which will be set
     */
    public void stackSet(int index, int value) {
        stack.set(index, value);
    }

    /**
     * Adds a value to the end of stack
     * @param value value for adding
     */
    public void stackAdd(int value) {
        stack.add(value);
    }

    /**
     * Removes element from stack by index
     * @param index index of object which will be removed
     * @return value of removed object
     */
    public int stackRemove(int index) {
        return stack.remove(index);
    }

    /**
     * Removes every element from stack
     */
    public void stackClear() {
        stack.clear();
    }

    /**
     * Allows to find out size of stack
     * @return stack size
     */
    public int stackSize() {
        return stack.size();
    }

    // Line section

    /**
     * Allows to get a string by index from current line
     * @param index index of needed string in {@code ArrayList}
     * @return string by index
     */
    public String lineGet(int index) {
        return line.get(index);
    }

    /**
     * Allows to set a string in {@code ArrayList} by index
     * @param index index for placing
     * @param value value for placing by index
     */
    public void lineSet(int index, String value) {
        line.set(index, value);
    }

    /**
     * Allows to add a string at the end if {@code ArrayList}
     * @param value string for adding
     */
    public void lineAdd(String value) {
        line.add(value);
    }

    /**
     * Allows to get a size of current line
     * @return size of line
     */
    public int lineSize() {
        return line.size();
    }

    /**
     * Allows to get a sublist by indexes from current line
     * @param beginIndex index for begin of array
     * @param endIndex index for end of array
     * @return list, created by your indexes from current line
     */
    public List<String> lineSubList(int beginIndex, int endIndex) {
        return line.subList(beginIndex, endIndex);
    }

    public void lineClear() {
        line.clear();
    }
}
