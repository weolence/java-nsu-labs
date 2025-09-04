package managers;

import exceptions.IllegalMessageTypeException;
import exceptions.NullSessionException;
import messages.*;
import messages.KeepAlive;
import network.Client;
import network.ClientSession;
import types.Envelope;
import types.MessageType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-directed class, required for thread-safe message sending by 
 * lining up queue and anti-spam message filtering.
 */
public class MessageSender implements Runnable {
    private final Map<byte[], Envelope> recentMessages = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Envelope> queue = new CopyOnWriteArrayList<>();
    private final Client client;
    private long lastTimeConnectionChecking = 0;
    private boolean running = true;

    /**
     * Creates instance of MessageSender, after end of
     * the construction & creation of run() thread class is 
     * fully ready for sending messages with send() method.
     * @param client is a provider of sessions needed for sending
     */
    public MessageSender(Client client) {
        this.client = client;
    }

    /**
     * Takes messages from queue one by one(from first) 
     * and sends them to given recipients, also in empty queue case 
     * initiates checking of connections, once in 10 seconds.
     */
    @Override
    public void run() {
        while(running) {
            if(queue.isEmpty()) {
                checkConnections();
                continue;
            }

            Envelope envelope = queue.removeFirst();
            byte[] recipientId = envelope.peerId();
            Message message = envelope.message();
            
            try {
                client.getSession(recipientId).write(message);
            } catch (IOException e) {
                System.out.println("unable to write message");
            } catch (NullSessionException e) {
                System.out.println(e.getMessage());
            }
            
        }
    }

    /**
     * Stops execution of runnable thread.
     */
    public void stop() { running = false; }

    /**
     * Adds envelope to queue with messages, 
     * then it will be sent to it's recipient.
     * @param envelope is taken message
     * @throws IllegalMessageTypeException in case of unsupported message type
     */
    public void send(Envelope envelope) throws IllegalMessageTypeException {
        if(!isSendingAcceptable(envelope)) return;
        queue.add(envelope);
        recentMessages.put(envelope.peerId(), envelope);
    }

    /**
     * Cancels sending of PIECE to given peer by deleting message
     * from sending queue in case message wasn't send already.
     * @param pieceIndex is index of PIECE which shouldn't be sent. Note that
     * if subpieces concept was used all subpieces of given index will be removed
     * @param peerId is id of recipient peer
     */
    public void cancelPieceSending(int pieceIndex, byte[] peerId) {
        for(Envelope envelope : queue) {
            MessageType type = envelope.message().getType();
            if(type != MessageType.PIECE || !Arrays.equals(envelope.peerId(), peerId)) 
                continue;
            Piece piece = envelope.message().as(Piece.class);
            if(piece.getPieceIndex() == pieceIndex)
                queue.remove(envelope);
        }
    }

    /**
     * Once in few seconds sends KEEP_ALIVE messages 
     * to every valid session.
     */
    private void checkConnections() {
        if(System.currentTimeMillis() - lastTimeConnectionChecking < 10000) return;
        for(ClientSession session : client.getValidSessions()) {
            try {
                send(new Envelope(new KeepAlive(), session.getPeerId()));
            } catch (Exception e) {
                return;
            }
        }
        lastTimeConnectionChecking = System.currentTimeMillis();
    }

    /**
     * This function represents a filter for messages, in it you exactly
     * can decide which types of messages are allowed to be sended recently. For example:
     * KEEP_ALIVE supposed to be sended every time for connection checking, as a PIECE also, 
     * because it can be sended only after REQUEST as a response. 
     * However, REQUEST, by itself, can be sent too many times before 
     * PIECE will be written, so it can cause SocketChannel overflow.
     * @param envelope is message for type-checking
     * @return boolean decision about sending/resending given envelope,
     * false by default in case of 
     * @throws IllegalMessageTypeException in case of unhandled type
     */
    private boolean isSendingAcceptable(Envelope envelope) throws IllegalMessageTypeException {
        Message message = envelope.message();
        MessageType type = message.getType();
        byte[] recipientId = envelope.peerId();
        
        if(!recentMessages.containsKey(recipientId)) return true;
        Message recentMessage = recentMessages.get(recipientId).message();
        if(!type.equals(recentMessage.getType())) return true;

        switch(type) {
            case KEEP_ALIVE: return true;
            case CHOKE: return false;
            case UNCHOKE: return false;
            case INTERESTED: return false;
            case NOT_INTERESTED: return false;
            case HAVE: {
                Have have = message.as(Have.class);
                Have recentHave = recentMessage.as(Have.class);
                return have.getPieceIndex() == recentHave.getPieceIndex();
            }
            case BITMAP: return false;
            case REQUEST: {
                Request request = message.as(Request.class);
                Request recentRequest = recentMessage.as(Request.class);
                return !(request.getPieceIndex() == recentRequest.getPieceIndex() &&
                         request.getPieceOffset() == recentRequest.getPieceOffset());
            }
            case PIECE: return true;
            case CANCEL: return false;
            case HANDSHAKE: return false;
            default: throw new IllegalMessageTypeException();
        }
    }
}
