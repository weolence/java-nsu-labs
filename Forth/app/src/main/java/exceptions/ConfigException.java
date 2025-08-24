package exceptions;

public class ConfigException extends RuntimeException{
    public ConfigException(String info) {
        super(info);
    }
}
