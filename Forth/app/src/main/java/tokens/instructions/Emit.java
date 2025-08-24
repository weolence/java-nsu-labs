package tokens.instructions;

import context.Context;
import out_formatter.OutFormatter;
import tokens.Token;
import exceptions.StackException;

// Prints stack top as ASCII number
public class Emit implements Token {
    public void execute(Context context) {
        int stackSize = context.stackSize();
        if(stackSize == 0) {
            throw new StackException("stack underflow");
        }
        int num = context.stackGet(stackSize - 1);
        OutFormatter outFormatter = new OutFormatter();
        outFormatter.append(String.valueOf((char)num));
    }
}
