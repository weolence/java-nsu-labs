package network;

import exceptions.*;
import messages.*;

import java.io.IOException;

public class ServerSession {
    private final Peer peer;
    private final byte[] infoHash;
    private final byte[] srcPeerId;
    private byte[] destPeerId;
    private boolean isInterested = true;
    private boolean isValid = false;

    public ServerSession(Peer peer, byte[] infoHash, byte[] srcPeerId) {
        this.peer = peer;
        this.infoHash = infoHash;
        this.srcPeerId = srcPeerId;
    }

    public byte[] getPeerId() { return destPeerId; }

    public boolean isValid() { return peer.isConnected() && isValid; }

    public boolean isInterested() { return isInterested; }
    public void interest() { isInterested = true; }
    public void unInterest() { isInterested = false; }

    public Message read() throws IOException, IllegalMessageTypeException {
        return peer.read();
    }

    public void readSessionProtocol() throws IOException, InvalidHandshakeException {
        Handshake handshake = peer.readHandshake();
        if(handshake == null) return;

        if(!handshake.isValid(infoHash))
            throw new InvalidHandshakeException();

        destPeerId = handshake.getPeerId();

        peer.write(new Handshake(Handshake.getProtocol(), infoHash, srcPeerId));

        isValid = true;
    }
}
