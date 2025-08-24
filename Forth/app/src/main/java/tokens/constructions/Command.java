package tokens.constructions;

import context.Context;
import tokens.Token;
import exceptions.SyntaxException;

import java.util.ArrayList;

/**
 * {@code Command} defines a pair in map which takes place in {@code Forth}.
 * Throws {@code RuntimeException} in case of attempt to create a command without name/definition
 */
public class Command implements Token {
    public void execute(Context context) {
        int pc = context.programCounterGet();
        int lineSize = context.lineSize();
        if(pc + 1 >= lineSize || pc + 2 >= lineSize) {
            throw new SyntaxException("invalid syntax");
        }

        String name = context.lineGet(pc + 1);
        ArrayList<String> definition = new ArrayList<String>(context.lineSubList(pc + 2, lineSize));
        context.defineCommandPut(name, definition);

        context.programCounterSet(lineSize - 1);
    }
}
