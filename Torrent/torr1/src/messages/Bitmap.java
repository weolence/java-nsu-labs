package messages;

import types.MessageType;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Bitmap implements Message {
    private final MessageType type = MessageType.BITMAP;
    private final boolean[] bitmap;

    public Bitmap(boolean[] bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public MessageType getType() { return type; }

    @Override
    public ByteBuffer toBytes() {
        byte[] bits = new byte[(bitmap.length + 7) / 8];
        Arrays.fill(bits, (byte) 0);
        for(int i = 0; i < bitmap.length; i++)
            if(bitmap[i]) bits[i / 8] |= 1 << (7 - (i % 8));

        ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + bits.length);

        buffer.putInt(1 + bits.length);
        buffer.put((byte) type.getId());
        buffer.put(bits);
        buffer.flip();

        return buffer;
    }

    public boolean[] getBitmap(int piecesAmount) { 
        return Arrays.copyOfRange(bitmap, 0, piecesAmount);
    }
}
