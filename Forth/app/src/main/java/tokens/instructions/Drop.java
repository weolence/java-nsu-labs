package tokens.instructions;

import context.Context;
import tokens.Token;
import exceptions.StackException;

// Deletes top of stack
public class Drop implements Token {
    public void execute(Context context) {
        int stackSize = context.stackSize();
        if(stackSize == 0) {
            throw new StackException("stack underflow");
        }
        context.stackRemove(stackSize - 1);
    }
}
