import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        try(InputStream stream = Main.class.getResourceAsStream("program.txt")) {
            if(stream == null) {
                System.out.println("no such a file");
                return;
            }
            Forth forth = new Forth(System.in);
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
