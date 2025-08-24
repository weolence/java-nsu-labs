import context.Context;
import out_formatter.OutFormatter;
import tokens.Token;
import data_types.MutableInt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * {@code Forth} compiler class which uses in it {@code Factory} and {@code OutFormatter}.
 * The main concept contained in endless cycle:
 *     <p>1) take input from Console/File</p>
 *     <p>2) parse that input</p>
 *     <p>3) interpret input split to array of tokens</p>
 *     <p>4) print output(if you got it)</p>
 * Also during the running of {@code Forth} you can define your own command, they will
 * be written at definedCommands(map).
 * Self-defined command prioritized in compare with basic commands
 * @see #definedCommands
 */
public class Forth {
    private final Map<String, ArrayList<String>> definedCommands = new HashMap<>();
    private final Factory factory = new Factory();
    private final ArrayList<Integer> stack = new ArrayList<>();
    private final ArrayList<String> line = new ArrayList<>();
    private final BufferedReader reader;
    private static final Logger logger = LogManager.getLogger(Forth.class);

    /**
     * Immediately starts cycle without exceptions
     * @param stream Allows us to read from input
     */
    public Forth(InputStream stream) {
        reader = new BufferedReader(new InputStreamReader(stream));
        logger.debug("Created {} for reading from console/file", reader);
        run();
    }

    /**
     * Mainly, works with input/output and initialisation of needed
     * calculations after matching a token
     */
    private void run() {
        OutFormatter outFormatter = new OutFormatter();
        try {
            logger.debug("Trying to configure Factory");
            factory.configure("config.properties");
        } catch(Exception exception) {
            outFormatter.printError(exception);
            return;
        }

        while(true) {
            outFormatter.printReady();
            logger.debug("Trying to parse line from {}", reader);
            if(!parse()) {
                logger.debug("No more lines for parsing in {}", reader);
                break;
            }

            logger.debug("Preprocessing line to standard template");
            preprocess();

            try {
                logger.debug("Starting interpretation of line");
                interpret();
                logger.debug("Interpretation ended successfully");
                outFormatter.printAnswer();
                outFormatter.printAcceptance();
                if(!outFormatter.isEmpty()) {
                    outFormatter.printAnswer();
                    outFormatter.flush();
                    logger.debug("Buffer of {} flushed", outFormatter);
                }
            } catch(Exception exception) {
                outFormatter.printError(exception);
                logger.error("Interpretation went wrong with exception", exception);
                outFormatter.clear();
            }

            for(int val : stack) {
                outFormatter.append(Integer.toString(val));
            }
            outFormatter.flush();
            logger.debug("Current stack printed");
        }
    }

    /**
     * Literally writes a definition of token to a line for interpretation
     * in case it was found in map a bit earlier
     * @param index Shows a place in program, in which definition of token should be placed
     */
    private void inline(MutableInt index) {
        logger.debug("Trying to get definition of self-defined token in {}", definedCommands);
        ArrayList<String> definition = definedCommands.get(line.get(index.get()));

        logger.debug("Placing definition in current token");
        line.addAll(index.get() + 1, definition);
        line.remove(index.get());

        index.decrementAndGet();
    }

    /**
     * Interprets line of tokens and calls for {@code Factory} in
     * case of matching key
     */
    private void interpret() {
        MutableInt i = new MutableInt(0);
        for(; i.get() < line.size(); i.incrementAndGet()) {
            int index = i.get();

            Predicate<String> isNumeric = (str) -> {
                try {
                    Integer.parseInt(str);
                    return true;
                } catch(Exception exception) {
                    return false;
                }
            };

            logger.debug("Checking if current token is num");
            if(isNumeric.test(line.get(index))) {
                stack.add(Integer.parseInt(line.get(index)));
                logger.debug("Current token is num and it added to stack successfully");
                continue;
            }

            logger.debug("Checking if current token is self-defined command");
            if(definedCommands.containsKey(line.get(index))) {
                inline(i);
                logger.debug("Current token is self-defined command and definition inlined successfully");
                continue;
            }

            logger.debug("Token is one of basic commands");
            logger.debug("Creating a context for executing that command");
            Context context = new Context(definedCommands, i, stack, line);
            Token tokenClass;
            try {
                logger.debug("Trying to create Token class by token {}", line.get(index));
                tokenClass = (Token)factory.create(line.get(index));
            } catch(Exception exception) {
                logger.error("Token unresolved with exception",exception);
                throw new RuntimeException("invalid syntax");
            }
            logger.debug("Trying to execute command by token");
            tokenClass.execute(context);
            logger.debug("Token resolved successfully");
        }
    }

    /**
     * Converts line after parsing to standard format
     * This means that there won't be any whitespaces in line
     * except cases like writing string operator(." string")
     */
    private void preprocess() {
        boolean flag = false;
        for(int i = 0; i < line.size(); ++i) {
            String content = line.get(i);
            if(content.equals(".\"") || content.equals("\"")) {
                flag = !flag;
                continue;
            }
            if(!flag && content.equals(" ")) {
                line.remove(i);
                i--;
            }
        }
        logger.debug("Preprocessing ended successfully");
    }

    /**
     * Creates line, based on current string from input and then
     * split it into tokens, which will be interpreted later
     * @return True in case of successful read. If there are no lines
     * left it will return false
     */
    private boolean parse() {
        String line;

        try {
            logger.debug("Getting a line from input");
            line = reader.readLine();
        } catch(IOException exception) {
            logger.error("Attempt of getting line failed with exception", exception);
            return false;
        }

        if(null == line) {
            logger.debug("Line is empty");
            return false;
        }

        String[] tokens = line.split("(?<=\\s)|(?=\\s)"); // Splitting by whitespaces
        logger.debug("Line split by whitespaces to array of Strings");
        this.line.clear();
        this.line.addAll(Arrays.asList(tokens));

        logger.debug("Parsing went successfully");
        return true;
    }
}
