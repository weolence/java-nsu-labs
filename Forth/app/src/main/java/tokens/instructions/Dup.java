package tokens.instructions;

import context.Context;
import tokens.Token;
import exceptions.StackException;

// Dubbing top of stack on stack
public class Dup implements Token {
    public void execute(Context context) {
        int stackSize = context.stackSize();
        if(stackSize == 0) {
            throw new StackException("stack underflow");
        }
        context.stackAdd(context.stackGet(stackSize - 1));
    }
}
