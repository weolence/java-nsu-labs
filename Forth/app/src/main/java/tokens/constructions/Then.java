package tokens.constructions;

import context.Context;
import tokens.Token;
import exceptions.SyntaxException;

public class Then implements Token {
    public void execute(Context context) {
        int pc = context.programCounterGet();
        int depth = 1;
        for(int i = pc - 1; depth != 0 && i >= 0; --i) {
            String content = context.lineGet(i);
            if(content.equals("then")) {
                depth++;
            } else if(content.equals("if")) {
                depth--;
            }
        }

        if(depth != 0 || pc + 1 >= context.lineSize() || !context.lineGet(pc + 1).equals(";")) {
            throw new SyntaxException("invalid syntax");
        }

        context.programCounterSet(pc + 1);
    }
}
