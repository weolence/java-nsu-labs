package managers;

import exceptions.TorrentMetadataException;
import exceptions.TorrentParsingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import com.turn.ttorrent.bcodec.*; // library for parsing

/**
 * Represents a .torrent file with all it's meta-data.
 * That class gives an access to all required data for 
 * creation of real file for downloading or comparing it
 * with expected one.
 */
public class TorrentFileManager {
    private final Path path;
    private String name;
    private int pieceLength;
    private int pieceAmount;
    private byte[] piecesHash;
    private byte[] infoHash;
    private long fileLength;

    /**
     * Creates instance of class which represents torrent file if there were no problems
     * with parsing meta-data from .torrent file. Otherwise class cannot
     * be used for getting info about torrent file.
     * @param path is path to .torrent from working project directory
     * @throws NoSuchAlgorithmException in case of troubles with hash function instantiation
     * @throws TorrentParsingException
     * @throws TorrentMetadataException
     * @throws IOException
     */
    public TorrentFileManager(Path path) throws NoSuchAlgorithmException, TorrentParsingException, TorrentMetadataException, IOException {
        this.path = path;
        parse();
        printInfo();
    } 

    /**
     * Gets path to file based on parsed meta-data.
     * @return path to parsed file's name from current working directory
     */
    public Path getPathToFile() { return path.getParent().resolve(name); }
    /**
     * Gets amount of pieces from .torrent file.
     * @return amount of pieces in integer value
     */
    public int getPieceAmount() { return pieceAmount; }
    /**
     * Gets length of one piece from .torrent file.
     * @return piece length in integer value
     */
    public int getPieceLength() { return pieceLength; }
    /**
     * Gets length of whole file from .torrent file
     * @return length of file in long value
     */
    public long getFileLength() { return fileLength; }
    /**
     * Gets info hash based on hash of map with meta-data.
     * Required for identification of correct peers, because same .torrent
     * files produce same info hash.
     * @return info hash contained in 20 bytes byte[]
     */
    public byte[] getInfoHash() { return infoHash; }

    /**
     * Required for checking pieces by their hash, expected hash lies
     * in parsed from .torrent meta-data and real hash calculates based on given params.
     * @param index is index of piece
     * @param pieceData is raw data of piece
     * @return true in case of hash equality
     * @throws NoSuchAlgorithmException in case of troubles with hash 
     * function instantiation
     */
    public boolean comaparePieceHash(int index, byte[] pieceData) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] pieceHash = sha1.digest(pieceData);
        return Arrays.equals(pieceHash, getPieceHash(index));
    }

    /**
     * Parses meta-data from given .torrent file by reading special map.
     * @throws TorrentParsingException in case of troubles with reading raw bytes
     * from .torrent file, which exactly contain map with meta-data
     * @throws TorrentMetadataException in case of troubles with getting required fields
     * from parsed map with meta-data
     * @throws NoSuchAlgorithmException in case of troubles with hash
     * function instantiation
     * @throws IOException in case of troubles with getting map's bytes for
     * info hash calculating
     */
    private void parse() throws TorrentParsingException, TorrentMetadataException, NoSuchAlgorithmException, IOException {
        byte[] bytes = null;
        Map<String, BEValue> top = null;
        Map<String, BEValue> info = null;
        try {
            bytes = Files.readAllBytes(path);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            top = BDecoder.bdecode(buffer).getMap();
            info = top.get("info").getMap();
        } catch (Exception e) {
            throw new TorrentParsingException();
        }

        try {
            name = info.get("name").getString();
            pieceLength = info.get("piece length").getInt();
            piecesHash = info.get("pieces").getBytes();  // 20*N bytes
            fileLength = info.get("length").getLong();
        } catch (Exception e) {
            throw new TorrentMetadataException();
        }

        ByteBuffer buffer = BEncoder.bencode(info);
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        infoHash = sha1.digest(buffer.array());
        pieceAmount = (int) Math.ceil((double) fileLength / pieceLength); 
    }

    /**
     * Gets hash of piece by give index.
     * @param index is piece number
     * @return hash of given piece in 20 bytes byte[]
     */
    private byte[] getPieceHash(int index) {
        if(index < 0 || index >= pieceAmount) {
            System.out.println("wrong index of piece");
            return null;
        }

        int pieceHashLen = piecesHash.length / pieceAmount; 
        return Arrays.copyOfRange(piecesHash, index * pieceHashLen, (index + 1) * pieceHashLen);
    }

    /**
     * Prints all meta-data parsed from .torrent file.
     */
    private void printInfo() {
        System.out.println("parsed metadata:");
        System.out.println("-name: " + name);
        System.out.println("-pieces amount: " + pieceAmount);
        System.out.println("-piece length: " + pieceLength);
        System.out.println("-file length: " + fileLength);
        // System.out.println("-hash of pieces:");
        // for(byte b : piecesHash)
        //     System.out.print(b + " ");
        System.out.println();
        System.out.println();
    }
}
