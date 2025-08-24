package messages;

import types.MessageType;

import java.nio.ByteBuffer;

public class Have implements Message {
    private final MessageType type = MessageType.HAVE;
    private final int pieceIndex;

    public Have(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    @Override
    public MessageType getType() { return type; }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + 4);

        buffer.putInt(1 + 4);
        buffer.put((byte) type.getId());
        buffer.putInt(pieceIndex);
        buffer.flip();

        return buffer; 
    }

    public int getPieceIndex() { return pieceIndex; }
}
