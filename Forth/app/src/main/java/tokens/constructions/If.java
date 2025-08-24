package tokens.constructions;

import context.Context;
import tokens.Token;
import exceptions.*;

public class If implements Token {
    public void execute(Context context) {
        int pc = context.programCounterGet();
        int lineSize = context.lineSize();
        int elseIndex = 0;
        int thenIndex = 0;
        int depth = 1;
        for(int i = pc + 1; depth != 0 && i < lineSize; ++i) {
            String content = context.lineGet(i);
            if(content.equals("if")) {
                depth++;
            } else if(content.equals("then")) {
                depth--;
                thenIndex = i;
            }
            if(depth == 1 && content.equals("else")) {
                elseIndex = i;
            }
        }

        if(depth != 0 || thenIndex + 1 >= lineSize || !context.lineGet(thenIndex + 1).equals(";")) {
            throw new SyntaxException("invalid syntax");
        }

        int stackSize = context.stackSize();
        if(stackSize == 0) {
            throw new StackException("stack underflow");
        }

        if(context.stackRemove(stackSize - 1) == 0) {
            if(elseIndex != 0) {
                context.programCounterSet(elseIndex);
            } else {
                context.programCounterSet(thenIndex - 1);
            }
        }
    }
}
