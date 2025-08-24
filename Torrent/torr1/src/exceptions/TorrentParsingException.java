package exceptions;

public class TorrentParsingException extends Exception {
    public TorrentParsingException() {
        super("unable to parse .torrent file");
    }    
}
