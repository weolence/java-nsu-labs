package types;

public enum MessageType {
    KEEP_ALIVE(0),
    CHOKE(1),
    UNCHOKE(2),
    INTERESTED(3),
    NOT_INTERESTED(4),
    HAVE(5),
    BITMAP(6),
    REQUEST(7),
    PIECE(8),
    CANCEL(9),
    HANDSHAKE(10);

    private final int id;

    MessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MessageType fromId(int id) {
        for (MessageType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown message type id: " + id);
    }
}

/*
 * Message type:
 * <[length] - 4 bytes for int><[message ID] - 1 byte><[payload] - (length-1) bytes>
 *
 * Types of messages:
 * 0: choke(prohibits data sharing)
 * 1: unchoke(allows data sharing)
 * 2: interested(in downloading, payload empty)
 * 3: not interested(in downloading, payload empty)
 * 4: have(having piece, payload is index)
 * 5: bitmap(payload is array)
 * 6: request(payload is <index pof piece><offset in file><length of piece>)
 * 7: piece(payload is <index of piece><offset in file><data of piece>)
 * 8: cancel(filled like request)
*/
