package network;

import exceptions.NullSessionException;

import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents client part of Torrent required for creating connections
 * and reconnecting to earlier created connections without loosing sessions.
 * Client also holds lists of current valid sessions and access getting
 * them by their id.
 */
public class Client implements Runnable {
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    private final ArrayList<String> addresses;
    private final byte[] infoHash;
    private final byte[] srcPeerID;
    private final boolean[] srcBitmap;
    private boolean running = true;

    /**
     * Creates instance of Client by given data.
     * @param addresses is array with addresses of given peers
     * @param infoHash is info hash required for checking .torrent correctness
     * @param srcPeerID is peer id of current client
     * @param srcBitmap is local bitmap which will be shared with other peers 
     */
    public Client(ArrayList<String> addresses, byte[] infoHash, byte[] srcPeerID, boolean[] srcBitmap) {
        this.addresses = addresses;
        this.infoHash = infoHash;
        this.srcPeerID = srcPeerID;
        this.srcBitmap = srcBitmap;
    }

    /**
     * Cycle for creating new connections by given
     * addresses and reconnecting to given peers.
     */
    @Override
    public void run() {
        while(running) {
            for(String address : addresses) {
                ClientSession session = sessions.get(address);

                if(session == null) {
                    SocketChannel channel;
                    try {
                        channel = convertAdressToChannel(address);
                    } catch (Exception e) {
                        continue;
                    }

                    Peer peer = new Peer(channel);
                    session = new ClientSession(peer, infoHash, srcPeerID, srcBitmap);
                    sessions.put(address, session);
                }
                
                if(session.isValid()) continue;
                
                try {
                    session.readSessionProtocol();
                } catch (Exception e) {
                    sessions.remove(address);
                    continue;
                }
            }
        }
    }

    /**
     * Stops execution of runnable thread.
     */
    public void stop() { running = false; }

    /**
     * Through all sessions, maintained by Client, finds session with
     * required peerId or throws exception in alternative case.
     * @param peerId is id of required session
     * @return session by given id
     * @throws NullSessionException in case of absence of required session
     */
    public ClientSession getSession(byte[] peerId) throws NullSessionException {
        Collection<ClientSession> allSessions = sessions.values();
        for(ClientSession session : allSessions)
            if(Arrays.equals(session.getPeerId(), peerId))
                return session;
        throw new NullSessionException();
    }

    /**
     * Gets collection of valid sessions, the main difference with getSession()
     * method is that if there is a needance of iterating through all sessions
     * they must be fully configured and connected. Whereas if you just want to 
     * mark session as chocked or reconnect it without recreation of session it
     * will be not valid for some time.
     * @return list with valid(means fully connected and configured) sessions
     */
    public ArrayList<ClientSession> getValidSessions() {
        Collection<ClientSession> allSessions = sessions.values();
        ArrayList<ClientSession> validSessions = new ArrayList<>();
        for(ClientSession session : allSessions)
            if(session.isValid()) 
                validSessions.add(session);
        return validSessions;
    }

    /**
     * Converts given IPv4 address to SocketChannel. 
     * @param address is IPv4 address
     * @return channel by given adress
     * @throws IOException in case of troubles with channel instantiation
     */
    private SocketChannel convertAdressToChannel(String address) throws IOException {
        String[] parts = address.split(":");
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
        channel.configureBlocking(false);
        return channel;
    }
}
