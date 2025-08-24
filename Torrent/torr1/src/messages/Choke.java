package messages;

import types.MessageType;

import java.nio.ByteBuffer;

public class Choke implements Message {
    private final MessageType type = MessageType.CHOKE;

    @Override
    public MessageType getType() { return type; }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 1);

        buffer.putInt(1); 
        buffer.put((byte) type.getId());
        buffer.flip();

        return buffer;
    }
}
