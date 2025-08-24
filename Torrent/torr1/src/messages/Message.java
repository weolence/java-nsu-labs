package messages;

import types.MessageType;

import java.nio.ByteBuffer;

public interface Message {
    int length = 4;

    MessageType getType();
    ByteBuffer toBytes();
    
    default <T extends Message> T as(Class<T> clazz) {
        if (clazz.isInstance(this)) return clazz.cast(this);
        throw new ClassCastException("cannot convert " + getType() + " to " + clazz.getSimpleName());
    }
}
