package managers;

import exceptions.BitmapConfigurationException;
import exceptions.IllegalMessageTypeException;
import exceptions.InvalidPieceDataException;
import exceptions.NullSessionException;
import messages.*;
import network.Client;
import network.Server;
import types.Envelope;
import types.MessageType;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Takes messages, received from Server and handles them(reacts on them)
 * in different ways. TorrentFile required for access to parsed meta-data,
 * File required for writing/reading operations to local file, MessageSender
 * sends answers to recieved REQUEST and etc. and Client&Server required for
 * getting different sessions.
 */
public class MessageHandler implements Runnable {
    private final FileManager file;
    private final TorrentFileManager torrentFile;
    private final MessageSender messageSender;
    private final Client client;
    private final Server server;
    private boolean running = true;

    /**
     * Creates example of message handler, which, after
     * instantiation, is fully ready for using as a runnable thread.
     */
    public MessageHandler(TorrentFileManager torrentFile, FileManager file, MessageSender messageSender, Client client, Server server) {
        this.torrentFile = torrentFile;
        this.file = file;
        this.messageSender = messageSender;
        this.client = client;
        this.server = server;
    }

    /**
     * Takes messages from queue provided by Server one by one
     * and handles them one by one with different reactions.
     */
    @Override
    public void run() {
        CopyOnWriteArrayList<Envelope> queue = server.getMessageQueue();
        while(running) {
            try {
                handle(queue.removeFirst());
            } catch (NoSuchElementException e) {
                continue;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
        }
    }
    
    /**
     * Stops execution of runnable thread.
     */
    public void stop() { running = false; }

    /**
     * Contains different reactions on different types of message.
     * @param envelope is received message which will be handled
     * @throws IllegalMessageTypeException in case of unsupported message type
     * @throws NullSessionException in case of troubles with getting session by peerId,
     * it means that now no available sessions for that peerId
     * @throws IOException in case of input/output troubles during writing/reading 
     * opertions with torrent file
     * @throws InvalidPieceDataException in case hash of received piece not equal to 
     * expected hash of same piece taken from .torrent meta data
     * @throws NoSuchAlgorithmException in case of troubles with hash function instantiation
     * @throws BitmapConfigurationException in case bitmap for one of peers wasn't received or
     * configured for some reasons and now can't be modified
     */
    private void handle(Envelope envelope) throws IllegalMessageTypeException, NullSessionException, IOException, InvalidPieceDataException, NoSuchAlgorithmException, BitmapConfigurationException {
        Message message = envelope.message();
        MessageType type = message.getType();
        byte[] peerId = envelope.peerId();

        switch(type) {
            case KEEP_ALIVE: handleKeepAlive(); break;
            case CHOKE: handleChoke(peerId); break;
            case UNCHOKE: handleUnchoke(peerId); break;
            case INTERESTED: handleInterested(peerId); break;
            case NOT_INTERESTED: handleNotInterested(peerId); break;
            case HAVE: handleHave(message, peerId); break;
            case BITMAP: handleBitmap(message, peerId); break;
            case REQUEST: handleRequest(message, peerId); break;
            case PIECE: handlePiece(message, peerId); break;
            case CANCEL: handleCancel(message, peerId); break;
            default: throw new IllegalMessageTypeException();
        }
    }

    private void handleKeepAlive() { }

    private void handleChoke(byte[] peerId) throws NullSessionException {
        client.getSession(peerId).choke();
    }

    private void handleUnchoke(byte[] peerId) throws NullSessionException {
        client.getSession(peerId).unchoke();
    }

    private void handleInterested(byte[] peerId) throws NullSessionException {
        server.getSession(peerId).interest();
    }

    private void handleNotInterested(byte[] peerId) throws NullSessionException {
        server.getSession(peerId).unInterest();
    }

    private void handleHave(Message message, byte[] peerId) throws BitmapConfigurationException, NullSessionException {
        Have have = message.as(Have.class);
        boolean[] bitmap = client.getSession(peerId).getBitmap();
        if(bitmap == null) throw new BitmapConfigurationException();
        bitmap[have.getPieceIndex()] = true;
    }

    private void handleBitmap(Message message, byte[] peerId) throws NullSessionException, IllegalMessageTypeException {
        Bitmap bitmap = message.as(Bitmap.class);
        boolean[] localBitmap = file.getBitmap();
        boolean[] receivedBitmap = bitmap.getBitmap(torrentFile.getPieceAmount());
        client.getSession(peerId).setBitmap(receivedBitmap);

        boolean isInterested = false;
        for(int i = 0; i < localBitmap.length && !isInterested; i++) {
            if(localBitmap[i] || !receivedBitmap[i]) continue;
            isInterested = true;
        }
        if(isInterested) messageSender.send(new Envelope(new Interested(), peerId));
        else messageSender.send(new Envelope(new NotInterested(), peerId));
        
        messageSender.send(new Envelope(new Unchoke(), peerId));
    }

    private void handleRequest(Message message, byte[] peerId) throws IOException, IllegalMessageTypeException {
        Request request = message.as(Request.class);
        int pieceIndex = request.getPieceIndex();
        int pieceOffset = request.getPieceOffset();
        int pieceLength = request.getPieceLength();

        byte[] pieceData = file.read(pieceIndex, pieceOffset, pieceLength);
        Piece piece = new Piece(pieceIndex, pieceOffset, pieceData);
        Envelope envelope = new Envelope(piece, peerId);

        messageSender.send(envelope);
    }

    private void handlePiece(Message message, byte[] peerId) throws IOException, InvalidPieceDataException, NoSuchAlgorithmException, IllegalMessageTypeException {
        Piece piece = message.as(Piece.class);
        int pieceIndex = piece.getPieceIndex();
        int pieceOffset = piece.getPieceOffset();
        byte[] pieceData = piece.getPieceData();

        if(!torrentFile.comaparePieceHash(pieceIndex, pieceData))
            throw new InvalidPieceDataException();
        file.write(pieceIndex, pieceOffset, pieceData);
        file.getBitmap()[pieceIndex] = true;

        messageSender.send(new Envelope(new Have(pieceIndex), peerId));
    }

    private void handleCancel(Message message, byte[] peerId) {
        Cancel cancel = message.as(Cancel.class);
        messageSender.cancelPieceSending(cancel.getPieceIndex(), peerId);
    }
}
