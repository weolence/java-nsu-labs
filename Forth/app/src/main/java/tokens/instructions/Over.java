package tokens.instructions;

import context.Context;
import tokens.Token;
import exceptions.StackException;

// Dubbing pre-top of stack to stack
public class Over implements Token {
    public void execute(Context context) {
        int stackSize = context.stackSize();
        if(stackSize < 2) {
            context.stackClear();
            throw new StackException("stack underflow");
        }
        context.stackAdd(context.stackGet(stackSize - 2));
    }
}
