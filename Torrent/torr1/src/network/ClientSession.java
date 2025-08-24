package network;

import exceptions.InvalidHandshakeException;
import messages.*;

import java.io.IOException;

public class ClientSession  {
    private final Peer peer;
    private final byte[] infoHash;
    private final byte[] srcPeerId;
    private final boolean[] srcBitmap;
    private boolean[] destBitmap;
    private byte[] destPeerId;
    private boolean isChoked = true;
    private boolean isValid = false;
    private boolean isHandshakeSent = false;

    public ClientSession(Peer peer, byte[] infoHash, byte[] srcPeerId, boolean[] srcBitmap) {
        this.peer = peer;
        this.infoHash = infoHash;
        this.srcPeerId = srcPeerId;
        this.srcBitmap = srcBitmap;
    }

    public byte[] getPeerId() { return destPeerId; }

    public boolean isValid() { return peer.isConnected() && isValid; }

    public boolean isChoked() { return isChoked; }
    public void choke() { isChoked = true; }
    public void unchoke() { isChoked = false; }

    public boolean[] getBitmap() { return destBitmap; }
    public void setBitmap(boolean[] bitmap) { destBitmap = bitmap; }

    public void write(Message message) throws IOException {
        try {
            peer.write(message);
        } catch (Exception e) {
            isValid = false;
            isHandshakeSent = false;
            throw new IOException();
        }
    }

    public void readSessionProtocol() throws IOException, InvalidHandshakeException {
        if(!isHandshakeSent) {
            write(new Handshake(Handshake.getProtocol(), infoHash, srcPeerId));
            isHandshakeSent = true;
        }

        Handshake handshake = peer.readHandshake();
        if(handshake == null) return;

        if(!handshake.isValid(infoHash))
            throw new InvalidHandshakeException();

        destPeerId = handshake.getPeerId();

        peer.write(new Bitmap(srcBitmap));

        isValid = true;
    }
}
