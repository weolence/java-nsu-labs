package exceptions;

public class TorrentMetadataException extends Exception {
    public TorrentMetadataException() {
        super("unable to get metadata from parsed dictionary");
    }
}
