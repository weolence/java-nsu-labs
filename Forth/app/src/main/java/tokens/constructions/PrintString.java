package tokens.constructions;

import context.Context;
import out_formatter.OutFormatter;
import tokens.Token;
import exceptions.SyntaxException;

public class PrintString implements Token {
    public void execute(Context context) {
        int pc = context.programCounterGet();
        if(pc + 1 >= context.lineSize() || !context.lineGet(pc + 1).equals(" ")) {
            throw new SyntaxException("invalid syntax");
        }

        StringBuilder out = new StringBuilder();
        int i = pc + 2;
        for(; i < context.lineSize() && !context.lineGet(i).contains("\""); ++i) {
            out.append(context.lineGet(i));
        }

        if(i == context.lineSize()) {
            throw new SyntaxException("invalid syntax");
        }

        StringBuilder lastToken = new StringBuilder(context.lineGet(i));
        lastToken.deleteCharAt(lastToken.length() - 1);

        out.append(lastToken);

        OutFormatter outFormatter = new OutFormatter();
        outFormatter.append(out.toString());

        context.programCounterSet(i);
    }
}
