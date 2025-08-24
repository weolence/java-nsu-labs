package tokens.instructions;

import context.Context;
import out_formatter.OutFormatter;
import tokens.Token;
import exceptions.StackException;

// Removes top of stack and prints it
public class DropAndPrint implements Token {
    public void execute(Context context) {
        int stackSize = context.stackSize();
        if(stackSize == 0) {
            throw new StackException("stack underflow");
        }
        OutFormatter outFormatter = new OutFormatter();
        outFormatter.append(Integer.toString(context.stackRemove(stackSize - 1)));
    }
}
