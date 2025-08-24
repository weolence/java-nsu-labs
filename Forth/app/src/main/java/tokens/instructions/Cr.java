package tokens.instructions;

import context.Context;
import out_formatter.OutFormatter;
import tokens.Token;

// Prints new line('\n')
public class Cr implements Token {
    public void execute(Context context) {
        OutFormatter outFormatter = new OutFormatter();
        outFormatter.append("\n");
    }
}
