package tokens.instructions;

import context.Context;
import tokens.Token;
import exceptions.StackException;

// Swaps top of stack with pre-top
public class Swap implements Token{
    public void execute(Context context) {
        int stackSize = context.stackSize();
        if(stackSize < 2) {
            context.stackClear();
            throw new StackException("stack underflow");
        }
        int temp = context.stackGet(stackSize - 1);
        context.stackSet(stackSize - 1, context.stackGet(stackSize - 2));
        context.stackSet(stackSize - 2, temp);
    }
}
