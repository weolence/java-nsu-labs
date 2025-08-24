package tokens.constructions;

import context.Context;
import tokens.Token;
import exceptions.*;

/**
 * In any case of matching cycle token {@code Do}, method from that will be
 * called only once. After that call at the end of string will be placed a sequence:
 * .... [beginIndex] [limitIndex] [i] [indexes of i] <- end of line
 * As you see, it allows us to figure out indexes which contain i for replacing it then.
 * After end of cycle sequence will be erased. Also, in case of nested loops it just will
 * create another sequences, but all of them will be erased after end of cycle.
 */
public class Do implements Token {
    public void execute(Context context) {
        int pc = context.programCounterGet();
        int lineSize = context.lineSize();
        int loopIndex = 0;
        int depth = 1;
        for(int i = pc + 1; depth != 0 && i < lineSize; ++i) {
            String content = context.lineGet(i);
            if(content.equals("do")) {
                depth++;
            } else if(content.equals("loop")) {
                depth--;
                loopIndex = i;
            }
        }

        if(depth != 0 || loopIndex == 0 || loopIndex + 1 >= lineSize || !context.lineGet(loopIndex + 1).equals(";")) {
            throw new SyntaxException("invalid syntax");
        }

        int stackSize = context.stackSize();
        if(stackSize < 2) {
            context.stackClear();
            throw new StackException("stack underflow");
        }

        if(context.stackGet(stackSize - 1) > context.stackGet(stackSize - 2)) {
            throw new SyntaxException("illegal bounds");
        }

        // Adding [beginIndex] [limitIndex] [i] [indexes of i] at the end of current line
        String beginIndex = Integer.toString(context.stackRemove(context.stackSize() - 1));
        context.lineAdd(beginIndex); // beginIndex
        context.lineAdd(Integer.toString(context.stackRemove(context.stackSize() - 1))); // limitIndex
        context.lineAdd(beginIndex); // i with beginIndex value
        context.lineAdd(""); // string which will contain every index of i in do-loop for replacing

        context.programCounterSet(loopIndex - 1);
    }
}
