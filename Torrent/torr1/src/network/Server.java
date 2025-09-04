package network;

import exceptions.InvalidServerChannelConfig;
import exceptions.NullSessionException;
import messages.Message;
import types.Envelope;

import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Server accepts and reads connections throug SocketChannels in
 * non-blocking mode. After reading a full message Server will wrap it into Envelope
 * with message & sender id. If connection lost server will cancel key associated 
 * with that connection and will register new if one appears.
 */
public class Server implements Runnable {
    private final CopyOnWriteArrayList<ServerSession> sessions = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Envelope> queue = new CopyOnWriteArrayList<>();
    private final int port;
    private final byte[] infoHash;
    private final byte[] peerId;
    private Selector selector = null;
    private ServerSocketChannel serverChannel = null;
    private boolean running = true;

    /**
     * Creates instance of Server by given data.
     * @param port needed for ServerChannel configuration
     * @param infoHash required for validating Handshake messages
     * @param peerId is unique machine id
     * @throws InvalidServerChannelConfig in case something went wrong during ServerChannel
     * configuration and Server cannot be used for receiving and accepting new connections
     */
    public Server(int port, byte[] infoHash, byte[] peerId) throws InvalidServerChannelConfig {
        this.port = port;
        this.infoHash = infoHash;
        this.peerId = peerId;
        init();
    }

    /** 
     * Cycle for accepting, reading and receiving new keys,
     * which contain putting read from SocketChannels messages into
     * queue for their handling then.
     */
    @Override
    public void run() {
        /*
         * TODO:
         * imagine a way interested peers may get benefits because in attempt of
         * waiting for interested peer's channels all non-blocking speed will be lost
         * since key appears after syscall(activity in SocketChannel detected) but not
         * in cycle
         */
        while(running) {
            try {
                selector.select();
            } catch(Exception e) {
                System.out.println("selector cannot find a single connection");
                return;
            }

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();

            while(iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if(!key.isValid()) {
                    key.cancel();
                } else if(key.isAcceptable()) {
                    try {
                        accept();
                    } catch (Exception e) {
                        System.out.println("acception of key failed");
                    }
                } else if(key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ServerSession session = (ServerSession) key.attachment();

                    if(session == null) {
                        Peer peer = new Peer(channel);
                        session = new ServerSession(peer, infoHash, peerId);
                        sessions.add(session);
                        key.attach(session);
                    }

                    if(!session.isValid()) {
                        try {
                            session.readSessionProtocol();
                        } catch (Exception e) {
                            System.out.println("unable to read session protocol");
                            sessions.remove(session);
                            key.cancel();
                            continue;
                        }
                    }

                    Message message;
                    try {
                        message = session.read();
                    } catch (Exception e) {
                        System.out.println("unable to read message");
                        sessions.remove(session);
                        key.cancel();
                        continue;
                    }
                    if(message == null) continue;

                    System.out.println("recieved: " + message.getClass().getName());

                    queue.add(new Envelope(message, session.getPeerId()));
                }
            }
        }
    }

    /**
     * Gives queue of read messages with id's of their senders, all
     * of them meant to be handled.
     * @return queue with messages
     */
    public CopyOnWriteArrayList<Envelope> getMessageQueue() { return queue; }

    /**
     * Stops execution of runnable thread.
     */
    public void stop() { running = false; }

    /**
     * Through all sessions, maintained by Server, finds session with
     * required peerId or throws exception in alternative case.
     * @param peerId is id of required session
     * @return session by given id
     * @throws NullSessionException in case of absence of required session
     */
    public ServerSession getSession(byte[] peerId) throws NullSessionException {
        for(ServerSession session : sessions)
            if(Arrays.equals(session.getPeerId(), peerId))
                return session;
        throw new NullSessionException();
    }

    /**
     * Checks if there any intrested in downloading sessions,
     * in case there is not a single interested session server 
     * can be stopped manualy using stop() method.
     * @return boolean result of finding interested session
     */
    public boolean isAnyIntrestedSessions() {
        for(ServerSession session : sessions)
            if(session.isInterested())
                return true;
        return false;
    }

    /**
     * Initiates server before starting main thread.
     * @throws InvalidServerChannelConfig in case of troubles with server cannel instantiation
     */
    private void init() throws InvalidServerChannelConfig {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch(Exception e) {
            running = false;
            throw new InvalidServerChannelConfig();
        }
    }

    /**
     * Accepts found connection and registers it in selector.
     * @throws IOException
     */
    private void accept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }
}
