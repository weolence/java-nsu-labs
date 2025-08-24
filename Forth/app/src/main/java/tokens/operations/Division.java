package tokens.operations;

import context.Context;
import tokens.Token;
import exceptions.StackException;

public class Division implements Token {
    public void execute(Context context) {
        if(context.stackSize() < 2) {
            context.stackClear();
            throw new StackException("stack underflow");
        }
        int rightOperand = context.stackRemove(context.stackSize() - 1);
        int leftOperand = context.stackRemove(context.stackSize() - 1);
        if(rightOperand == 0) {
            throw new RuntimeException("division by zero");
        }
        context.stackAdd(leftOperand / rightOperand);
    }
}
