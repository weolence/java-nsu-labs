package exceptions;

public class InvalidServerChannelConfig extends Exception {
    public InvalidServerChannelConfig() {
        super("creation of server channel went wrong due to invalid config data");
    }
}
