import context.Context;
import exceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import data_types.MutableInt;
import out_formatter.OutFormatter;
import tokens.Token;
import tokens.constructions.*;
import tokens.instructions.*;

import static org.junit.jupiter.api.Assertions.*;

public class ForthTests {
    private Context context = null;
    private OutFormatter outFormatter = null;

    private InputStream systemIn = null;
    private PrintStream systemOut = null;

    @BeforeEach
    void initialize() {
        MutableInt index = new MutableInt(0);
        Map<String, ArrayList<String>> map = new HashMap<>();
        ArrayList<Integer> stack = new ArrayList<>();
        ArrayList<String> line = new ArrayList<>();

        context = new Context(map, index, stack, line);
        outFormatter = new OutFormatter();

        systemIn = System.in;
        systemOut = System.out;
        System.setIn(context.inGet());
        System.setOut(context.outStreamGet());
    }
    @AfterEach
    void deInitialize() {
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    // COMPILER TESTS
    @Test
    void factorialTest() {
        Forth forth = new Forth(Forth.class.getResourceAsStream("factorialTest.txt"));
        String answer = "120";

        System.out.flush();

        assertTrue(context.outFlush().contains(answer));
    }

    // CONSTRUCTIONS TESTS PART
    @Test
    void commandTest() {
        Token commandClass = new Command();
        Exception exception;

        context.lineAdd(":");

        exception = assertThrows(Exception.class, () -> commandClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        String command = "command";
        context.lineAdd(command);

        exception = assertThrows(Exception.class, () -> commandClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        String token = "nonValidToken";
        context.lineAdd(token);

        commandClass.execute(context);

        assertEquals(token, context.definedCommandGet(command).get(0));
    }

    @Test
    void doTest() {
        Token doClass = new Do();
        Exception exception;

        context.lineAdd("do");

        exception = assertThrows(Exception.class, () -> doClass.execute(context));
        assertTrue(exception instanceof SyntaxException);
        
        context.lineAdd("loop");

        exception = assertThrows(Exception.class, () -> doClass.execute(context));
        assertTrue(exception instanceof SyntaxException);
        
        context.lineAdd(";");

        exception = assertThrows(Exception.class, () -> doClass.execute(context));
        assertTrue(exception instanceof StackException);
        
        context.stackAdd(6);
        context.stackAdd(4);

        doClass.execute(context);
    } 

    @Test
    void ifTest() {
        Token ifClass = new If();
        Exception exception;

        context.lineAdd("if");

        exception = assertThrows(Exception.class, () -> ifClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineAdd("else");

        exception = assertThrows(Exception.class, () -> ifClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineAdd("then");

        exception = assertThrows(Exception.class, () -> ifClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineAdd(";");

        exception = assertThrows(Exception.class, () -> ifClass.execute(context));
        assertTrue(exception instanceof StackException);

        context.stackAdd(2);

        ifClass.execute(context);
    }

    @Test
    void elseTest() {
        Token elseClass = new Else();
        Exception exception;

        context.lineAdd("else");

        exception = assertThrows(Exception.class, () -> elseClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineAdd("then");

        exception = assertThrows(Exception.class, () -> elseClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineAdd(";");

        exception = assertThrows(Exception.class, () -> elseClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineClear();
        context.lineAdd("if");
        context.lineAdd("else");
        context.lineAdd("then");
        context.lineAdd(";");
        context.programCounterSet(1);

        elseClass.execute(context);
    }

    @Test
    void thenTest() {
        Token thenClass = new Then();
        Exception exception;

        context.lineAdd("then");

        exception = assertThrows(Exception.class, () -> thenClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineAdd(";");

        exception = assertThrows(Exception.class, () -> thenClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineClear();
        context.lineAdd("if");
        context.lineAdd("else");
        context.lineAdd("then");
        context.lineAdd(";");
        context.programCounterSet(2);

        thenClass.execute(context);
    }

    // INSTRUCTIONS TESTS PART
    @Test
    void printStringTest() {
        Token printStringClass = new PrintString();
        Exception exception;

        context.lineAdd(".\"");

        exception = assertThrows(Exception.class, () -> printStringClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        context.lineAdd(" ");

        exception = assertThrows(Exception.class, () -> printStringClass.execute(context));
        assertTrue(exception instanceof SyntaxException);

        String message = " hello  world !! ! ";
        context.lineAdd(message + "\"");

        printStringClass.execute(context);
        outFormatter.flush();

        assertEquals(message + " \n", context.outFlush());
    }

    @Test
    void crTest() {
        Token crClass = new Cr();

        crClass.execute(context);
        outFormatter.flush();

        assertEquals("\n" + " \n", context.outFlush());
    }

    @Test
    void dropTest() {
        Token dropClass = new Drop();
        Exception exception;

        exception = assertThrows(Exception.class, () -> dropClass.execute(context));
        assertTrue(exception instanceof StackException);

        int testNumber = 1;
        context.stackAdd(testNumber);

        dropClass.execute(context);

        if(context.stackSize() == 0) {
            assertTrue(true);
        } else {
            assertNotEquals(testNumber, context.stackGet(context.stackSize() - 1));
        }
    }

    @Test
    void dropAndPrintTest() {
        Token dropAndPrintClass = new DropAndPrint();
        Exception exception;

        exception = assertThrows(Exception.class, () -> dropAndPrintClass.execute(context));
        assertTrue(exception instanceof StackException);

        int testNumber = 1;
        context.stackAdd(testNumber);

        dropAndPrintClass.execute(context);
        outFormatter.flush();

        assertEquals(testNumber + " \n", context.outFlush());
    }

    @Test
    void dupTest() {
        Token dupClass = new Dup();
        Exception exception;

        exception = assertThrows(Exception.class, () -> dupClass.execute(context));
        assertTrue(exception instanceof StackException);

        int testNumber = 1;
        context.stackAdd(testNumber);

        dupClass.execute(context);

        assertEquals(testNumber, context.stackGet(context.stackSize() - 1));
        assertEquals(testNumber, context.stackGet(context.stackSize() - 2));
    }

    @Test
    void overTest() {
        Token overClass = new Over();
        Exception exception;

        exception = assertThrows(Exception.class, () -> overClass.execute(context));
        assertTrue(exception instanceof StackException);

        int testNumber = 1;
        int trashNumber = 0;
        context.stackAdd(testNumber);

        exception = assertThrows(Exception.class, () -> overClass.execute(context));
        assertTrue(exception instanceof StackException);

        context.stackAdd(testNumber);
        context.stackAdd(trashNumber);

        overClass.execute(context);

        assertEquals(testNumber, context.stackGet(context.stackSize() - 1));
    }

    @Test
    void rotTest() {
        Token rotClass = new Rot();
        Exception exception;

        context.stackAdd(1);

        exception = assertThrows(Exception.class, () -> rotClass.execute(context));
        assertTrue(exception instanceof StackException);

        context.stackAdd(1);
        context.stackAdd(2);

        exception = assertThrows(Exception.class, () -> rotClass.execute(context));
        assertTrue(exception instanceof StackException);

        context.stackAdd(1);
        context.stackAdd(2);
        context.stackAdd(3);

        rotClass.execute(context);

        assertEquals(2, context.stackGet(2));
        assertEquals(1, context.stackGet(1));
        assertEquals(3, context.stackGet(0));
    }

    @Test
    void swapTest() {
        Token swapClass = new Swap();
        Exception exception;

        exception = assertThrows(Exception.class, () -> swapClass.execute(context));
        assertTrue(exception instanceof StackException);

        int testNumberA = 0;
        int testNumberB = 1;
        context.stackAdd(testNumberA);

        exception = assertThrows(Exception.class, () -> swapClass.execute(context));
        assertTrue(exception instanceof StackException);

        context.stackAdd(testNumberA);
        context.stackAdd(testNumberB);

        swapClass.execute(context);

        assertEquals(testNumberA, context.stackGet(context.stackSize() - 1));
        assertEquals(testNumberB, context.stackGet(context.stackSize() - 2));
    }
}
