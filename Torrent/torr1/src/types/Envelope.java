package types;

import messages.Message;

public record Envelope(Message message, byte[] peerId) { }
