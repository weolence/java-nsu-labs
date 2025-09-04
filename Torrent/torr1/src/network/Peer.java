package network;

import exceptions.IllegalMessageTypeException;
import messages.*;
import types.MessageType;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents connection to peer which allows receiving and
 * sending messages. This class hides low-level reading/writing
 * to socket channels.
 */
public class Peer {
    private final SocketChannel channel;
    private final ReadState msgLenState = new ReadState(Message.length);
    private final ReadState msgState = new ReadState(0);

    /**
     * Creates example of peer over given channel.
     * @param channel is channel which will be used for reading/writing
     */
    public Peer(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * Checks if channel fully connected & ready for reading/writing.
     * That method cannot signalize loss of connection, it only
     * says is channel given & is channel initialized correctly.
     * @return boolean value of described atributes
     */
    public boolean isConnected() { 
        return channel != null && channel.isConnected();
    }

    /**
     * Writes given message to peer's channel. Writing happens
     * in blocking mode because there are no reasons for stopping
     * writing without it's finishing.
     * @param message is message for writing
     * @throws IOException in case loss of connection or troubles with writing
     */
    public void write(Message message) throws IOException {
        ByteBuffer buffer = message.toBytes();
        while(buffer.hasRemaining())
            if(channel.write(buffer) < 0)
                throw new ClosedChannelException();
    }

    /**
     * Reads handshake in non-blocking mode from given channel. Method required for
     * reading different structure of message. Handshake built different from other
     * supportable types of messages.
     * @return handshake message or null in case handhsake wasn't read yet
     * @throws IOException in case of connection loss or any other troubles with reading
     */
    public Handshake readHandshake() throws IOException {
        if(!msgLenState.isStarted()) msgLenState.reset(Handshake.getProtocolSpaceLength());

        if(!msgLenState.isCompleted()) msgLenState.read(channel);
        if(!msgLenState.isCompleted()) return null;

        if(!msgState.isStarted()) {
            int len = (int) msgLenState.getBuffer().get();
            msgState.reset(len + Handshake.getReservedLength() 
            + Handshake.getInfoHashLength() + Handshake.getPeerIdLength());
        }

        if(!msgState.isCompleted()) msgState.read(channel);
        if(!msgState.isCompleted()) return null;

        ByteBuffer buffer = msgState.getBuffer();

        byte[] protocolBytes = new byte[Handshake.getProtocol().length];
        buffer.get(protocolBytes);

        byte[] reservedBytes = new byte[Handshake.getReservedLength()];
        buffer.get(reservedBytes);

        byte[] infoHash = new byte[Handshake.getInfoHashLength()];
        buffer.get(infoHash);

        byte[] peerId = new byte[Handshake.getPeerIdLength()];
        buffer.get(peerId);

        msgLenState.reset(Message.length);
        msgState.reset(0);

        return new Handshake(protocolBytes, infoHash, peerId);
    }

    /**
     * Reads message in non-blocking mode.
     * @return message or null in case message wasn't read yet
     * @throws IOException in case of connection loss or any other troubles with reading
     * @throws IllegalMessageTypeException in case of facing unsupported type of message
     */
    public Message read() throws IOException, IllegalMessageTypeException {
        if(!msgLenState.isStarted()) msgLenState.reset(Message.length);

        if(!msgLenState.isCompleted()) msgLenState.read(channel);
        if(!msgLenState.isCompleted()) return null;

        if(!msgState.isStarted()) {
            int len = msgLenState.getBuffer().getInt();
            msgState.reset(len);
        }

        if(!msgState.isCompleted()) msgState.read(channel);
        if(!msgState.isCompleted()) return null;

        ByteBuffer buffer = msgState.getBuffer();
        
        msgLenState.reset(Message.length);
        msgState.reset(0);

        return interpretateMessage(buffer);
    }

    /**
     * Interpretates message by read type and istantiates required message class for returning.
     * @param buffer is data read from channel
     * @return constructed message
     * @throws IllegalMessageTypeException in case of facing unsupported type of message
     */
    private Message interpretateMessage(ByteBuffer buffer) throws IllegalMessageTypeException {
        int dataLen = buffer.capacity();
        MessageType type = MessageType.fromId(buffer.get());
         
        Message message = switch(type) {
            case KEEP_ALIVE -> new KeepAlive();
            case CHOKE -> new Choke();
            case UNCHOKE -> new Unchoke(); 
            case INTERESTED -> new Interested();
            case NOT_INTERESTED -> new NotInterested();
            case HAVE -> {
                int pieceIndex = buffer.getInt();
                yield new Have(pieceIndex);
            }
            case BITMAP -> {
                byte[] bits = new byte[dataLen - 1];
                buffer.get(bits);
                // sometimes bitmap can contain more booleans than needed
                boolean[] bitmap = new boolean[bits.length * 8];
                for(int i = 0; i < bits.length * 8; i++)
                    bitmap[i] = (bits[i / 8] & 1 << (7 - (i % 8))) != 0 ? true : false;
                yield new Bitmap(bitmap);
            }
            case REQUEST -> {
                int pieceIndex = buffer.getInt();
                int pieceOffset  = buffer.getInt();
                int pieceLength = buffer.getInt();
                yield new Request(pieceIndex, pieceOffset, pieceLength);
            }
            case PIECE -> {
                int pieceIndex = buffer.getInt();
                int pieceOffset = buffer.getInt();
                byte[] pieceData = new byte[dataLen - 4 - 4 - 1];
                buffer.get(pieceData);
                yield new Piece(pieceIndex, pieceOffset, pieceData);
            }
            case CANCEL -> {
                int pieceIndex = buffer.getInt();
                int pieceOffset  = buffer.getInt();
                int pieceLength = buffer.getInt();
                yield new Cancel(pieceIndex, pieceOffset, pieceLength);
            }
            default -> null;
        };

        if(message == null) throw new IllegalMessageTypeException();

        return message;
    }

    /**
     * Class required for non-blocking reading from channel.
     */
    private class ReadState {
        private ByteBuffer buffer;
        private boolean isCompleted = false;
        private int bytesRead;

        public ReadState(int bufferSize) {
            this.buffer = ByteBuffer.allocate(bufferSize);
        }

        public ByteBuffer getBuffer() {
            return isCompleted ? buffer : null;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public boolean isStarted() {
            return bytesRead > 0;
        }

        public void reset(int bufferSize) {
            buffer = ByteBuffer.allocate(bufferSize);
            isCompleted = false;
            bytesRead = 0;
        }

        public void read(SocketChannel channel) throws IOException {
            if(isCompleted) return;

            int dataRead = channel.read(buffer);

            if(dataRead > 0) {
                bytesRead += dataRead;
            } else if(dataRead == -1) {
                isCompleted = true;
            }
            
            if(bytesRead >= buffer.capacity()) {
                isCompleted = true;
                buffer.flip();
            }
        }
    }
}
