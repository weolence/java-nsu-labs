package tokens.instructions;

import context.Context;
import tokens.Token;
import exceptions.StackException;

// Cyclic move 3 last position of stack
public class Rot implements Token {
    public void execute(Context context) {
        int stackSize = context.stackSize();
        if(stackSize < 3) {
            context.stackClear();
            throw new StackException("stack underflow");
        }
        int temp = context.stackGet(stackSize - 1);
        context.stackSet(stackSize - 1, context.stackGet(stackSize - 2));
        context.stackSet(stackSize - 2, context.stackGet(stackSize - 3));
        context.stackSet(stackSize - 3, temp);
    }
}
