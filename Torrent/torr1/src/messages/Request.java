package messages;

import types.MessageType;

import java.nio.ByteBuffer;

public class Request implements Message {
    private final MessageType type = MessageType.REQUEST;
    private final int pieceIndex;
    private final int pieceOffset;
    private final int pieceLength;

    public Request(int pieceIndex, int pieceOffset, int pieceLength) {
        this.pieceIndex = pieceIndex;
        this.pieceOffset = pieceOffset;
        this.pieceLength = pieceLength;
    }

    @Override
    public MessageType getType() { return type; }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + 4 + 4 + 4);

        buffer.putInt(1 + 4 + 4 + 4);
        buffer.put((byte) type.getId());
        buffer.putInt(pieceIndex);
        buffer.putInt(pieceOffset);
        buffer.putInt(pieceLength);
        buffer.flip();

        return buffer; 
    }

    public int getPieceIndex() { return pieceIndex; }
    public int getPieceOffset() { return pieceOffset; }
    public int getPieceLength() { return pieceLength; }
}
