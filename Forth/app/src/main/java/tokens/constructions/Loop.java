package tokens.constructions;

import context.Context;
import tokens.Token;
import exceptions.SyntaxException;

public class Loop implements Token {
    public void execute(Context context) {
        int pc = context.programCounterGet();
        int lineSize = context.lineSize();
        if(pc + 1 >= lineSize || !context.lineGet(pc + 1).equals(";")) {
            throw new SyntaxException("invalid syntax");
        }

        int doIndex = 0;
        int depth = 1;
        for(int i = pc - 1; depth != 0 && i >= 0; --i) {
            String content = context.lineGet(i);
            if(content.equals("loop")) {
                depth++;
            } else if(content.equals("do")) {
                depth--;
                doIndex = i;
            }
            if(content.equals("i") && depth == 1) {
                context.lineSet(lineSize - 1, context.lineGet(lineSize - 1) + i + " ");
            }
        }

        if(depth != 0) {
            throw new SyntaxException("invalid syntax");
        }

        if(context.lineGet(lineSize - 2).equals(context.lineGet(lineSize - 3))) {
            context.lineSubList(lineSize - 4, lineSize).clear();
            context.programCounterSet(pc + 1);
            return;
        }

        String content = context.lineGet(lineSize - 1);
        if(!content.isEmpty()) {
            String[] numbers = content.split("\\s+"); // Splitting by whitespaces
            for(String index : numbers) {
                context.lineSet(Integer.parseInt(index), context.lineGet(lineSize - 2));
            }
        }

        context.programCounterSet(doIndex);
        int currentIndex = Integer.parseInt(context.lineGet(lineSize - 2));
        currentIndex++;
        context.lineSet(lineSize - 2, Integer.toString(currentIndex));
    }
}
