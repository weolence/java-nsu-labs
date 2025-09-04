package managers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a manager, which access communication with local data
 * by reading/writing pieces of torrent file to disk.
 */
public class FileManager {
    private final TorrentFileManager torrentFile;
    private final File file;
    private boolean[] bitmap;

    /**
     * Creates instance of file manager.
     * @param torrentFile is representation of .torrent file, 
     * which can provide class with required meta-data.
     * @throws IOException in case of troubles with bitmap generation
     * because of writing/reading exceptions
     * @throws NoSuchAlgorithmException in case of troubles with hash function instantiation
     */
    public FileManager(TorrentFileManager torrentFile) throws NoSuchAlgorithmException, IOException {
        this.torrentFile = torrentFile;
        file = new File(torrentFile.getPathToFile().toString());
        if(!file.exists()) prepareFile();
        bitmap = buildBitmap();
    }

    /**
     * Returns once built bitmap
     * @return represented with boolean[] bitmap
     */
    public boolean[] getBitmap() { return bitmap; }

    /**
     * Specially for writing in files given by .torrent with given params. 
     * Method is friendly for subpieces writing.
     * @param pieceIndex is index of piece, which will be written
     * @param pieceOffset is offset in piece. For example, given piece
     * can be divided in few subpieces, so there is a needance of
     * moving through piece
     * @param data is data which will be written into file
     * @throws IOException in case of troubles with accessing to file or writing errors
     */
    public void write(int pieceIndex, int pieceOffset, byte[] data) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(pieceIndex * (long) torrentFile.getPieceLength() + pieceOffset);
            raf.write(data);
        } catch (Exception e) {
            throw new IOException("writing in file exception");
        }
    }

    /**
     * Specially for reading from files given by .torrent with given params.
     * Method is friendly for subpieces reading.
     * @param pieceIndex is index of piece, which will be read
     * @param pieceOffset is offset in piece. For example, given piece
     * can be divided in few subpieces, so there is a needance of
     * moving through piece
     * @param pieceDataLength is length of data, which will be read
     * @return byte[] with length of pieceDataLength, fully filled with data
     * @throws IOException in case of troubles with accessing to file or reading errors
     */
    public byte[] read(int pieceIndex, int pieceOffset, int pieceDataLength) throws IOException {
        byte[] buffer = null;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long offset = pieceIndex * (long) torrentFile.getPieceLength() + pieceOffset;
            raf.seek(offset);
            buffer = new byte[pieceDataLength];
            raf.readFully(buffer);
        } catch (Exception e) {
            throw new IOException("reading from file exception");
        }
        return buffer;
    }

    /**
     * Builds bitmap by compairing hash received from .torrent file
     * and calculated hash of locally available pieces. Bitmap represenets 
     * availability of pieces, required for file wholeness.
     * @return bitmap with size of piece amount from .torrent file
     * @throws IOException in case of troubles with accessing to file or reading errors
     * @throws NoSuchAlgorithmException in case of troubles with initiating sha1, required for
     * hash calculating
     */
    private boolean[] buildBitmap() throws IOException, NoSuchAlgorithmException {
        int pieceAmount = torrentFile.getPieceAmount();
        int pieceLength = torrentFile.getPieceLength();
        bitmap = new boolean[pieceAmount];
        for(int i = 0; i < pieceAmount; i++) {
            long offset = i * (long) pieceLength;
            int length = (int) (i == pieceAmount - 1 ? Math.min(pieceLength, torrentFile.getFileLength() - offset) : pieceLength);

            byte[] buffer = read(i, 0, length);

            bitmap[i] = torrentFile.comaparePieceHash(i, buffer);
        }
        return bitmap;
    }

    /**
     * Creates files and required directories(in path), where file
     * from .torrent will be written.
     */
    private void prepareFile() {
        file.getParentFile().mkdirs();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(torrentFile.getFileLength());
        } catch (Exception e) {
            System.out.println("unable to create file by path:");
            System.out.println(file.getPath());
        }
    }
}
