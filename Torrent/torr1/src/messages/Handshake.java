package messages;

import types.MessageType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Handshake implements Message {
    private final MessageType type = MessageType.HANDSHAKE;
    private static final String protocol = "BitTorrent protocol";
    private static final int protocolSpaceLength = 1;
    private static final int reservedLength = 8;
    private static final int infoHashLength = 20;
    private static final int peerIdLength = 20;
    private final byte[] protocolBytes;
    private final byte[] infoHash;
    private final byte[] srcPeerId;

    public Handshake(byte[] protocolBytes, byte[] infoHash, byte[] srcPeerId) {
        this.protocolBytes = protocolBytes;
        this.infoHash = infoHash;
        this.srcPeerId = srcPeerId;
    }

    public static byte[] getProtocol() { return protocol.getBytes(); }
    public static int getProtocolSpaceLength() { return protocolSpaceLength; }
    public static int getReservedLength() { return reservedLength; }
    public static int getInfoHashLength() { return infoHashLength; }
    public static int getPeerIdLength() { return peerIdLength; }
    public byte[] getInfoHash() { return infoHash; }
    public byte[] getPeerId() { return srcPeerId; }

    @Override
    public MessageType getType() { return type; }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(protocolSpaceLength + 
        protocol.getBytes().length + reservedLength + infoHashLength + peerIdLength);
        byte[] protocolBytes = protocol.getBytes(StandardCharsets.UTF_8);

        buffer.put((byte) protocolBytes.length);
        buffer.put(protocolBytes);
        buffer.put(new byte[reservedLength]);
        buffer.put(infoHash);
        buffer.put(srcPeerId);
        buffer.flip();

        return buffer;
    }

    public boolean isValid(byte[] requiredInfoHash) {
        return Arrays.equals(requiredInfoHash, infoHash) && 
        Arrays.equals(protocolBytes, protocol.getBytes());
    }
}