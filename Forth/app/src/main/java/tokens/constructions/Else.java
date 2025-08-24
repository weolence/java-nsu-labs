package tokens.constructions;

import context.Context;
import tokens.Token;
import exceptions.SyntaxException;

public class Else implements Token {
    public void execute(Context context) {
        int pc = context.programCounterGet();
        int depth = 1;
        for(int i = pc - 1; depth != 0 && i >= 0; --i) {
            String content = context.lineGet(i);
            if(content.equals("then")) {
                depth++;
            } else if (content.equals("if")) {
                depth--;
            }
        }

        if(depth != 0) {
            throw new SyntaxException("invalid syntax");
        }

        int lineSize = context.lineSize();
        int thenIndex = 0;
        depth = 1;
        for(int i = pc + 1; depth != 0 && i < lineSize; ++i) {
            String content = context.lineGet(i);
            if(content.equals("if")) {
                depth++;
            } else if (content.equals("then")) {
                thenIndex = i;
                depth--;
            }
        }

        if(depth != 0 || thenIndex + 1 >= lineSize || !context.lineGet(thenIndex + 1).equals(";")) {
            throw new SyntaxException("invalid syntax");
        }

        context.programCounterSet(thenIndex - 1);
    }
}
