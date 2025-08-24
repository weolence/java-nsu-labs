package messages;

import types.MessageType;

import java.nio.ByteBuffer;

public class Piece implements Message {
    private final MessageType type = MessageType.PIECE;
    private final int pieceIndex;
    private final int pieceOffset;
    private final byte[] pieceData;

    public Piece(int pieceIndex, int pieceOffset, byte[] pieceData) {
        this.pieceIndex = pieceIndex;
        this.pieceOffset = pieceOffset;
        this.pieceData = pieceData;
    }

    @Override
    public MessageType getType() { return type; }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + 4 + 4 + pieceData.length);

        buffer.putInt(1 + 4 + 4 + pieceData.length);
        buffer.put((byte) type.getId());
        buffer.putInt(pieceIndex);
        buffer.putInt(pieceOffset);
        buffer.put(pieceData);
        buffer.flip();

        return buffer; 
    }

    public int getPieceIndex() { return pieceIndex; }
    public int getPieceOffset() { return pieceOffset; }
    public byte[] getPieceData() { return pieceData; }
}
