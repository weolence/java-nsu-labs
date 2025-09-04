import exceptions.IllegalMessageTypeException;
import managers.*;
import messages.*;
import network.*;
import types.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Torrent {
    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("Required arguments:");
            System.out.println("arg[0] - path to *.torrent,");
            System.out.println("arg[1] - port for server,");
            System.out.println("arg[2] - 1st peer, ...");
            return;
        }

        Path path = Paths.get(args[0]);
        int port = Integer.parseInt(args[1]);
        ArrayList<String> addresses = new ArrayList<>(Arrays.asList(args).subList(2, args.length));

        TorrentFileManager torrentFile;
        FileManager file;
        try {
            torrentFile = new TorrentFileManager(path);
            file = new FileManager(torrentFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        byte[] infoHash = torrentFile.getInfoHash();
        byte[] peerId = getPeerId();
        boolean[] bitmap = file.getBitmap();

        Server server = null;
        try {
            server = new Server(port, infoHash, peerId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        Client client = new Client(addresses, infoHash, peerId, bitmap);
        MessageSender messageSender = new MessageSender(client);
        MessageHandler messageHandler = new MessageHandler(torrentFile, file, messageSender, client, server);

        ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS, workQueue);

        executor.submit(server);
        executor.submit(client);
        executor.submit(messageHandler);
        executor.submit(messageSender);
        

        int pieceAmount = torrentFile.getPieceAmount();
        for(int i = 0; i < pieceAmount;) {
            if(bitmap[i]) {
                i++;
                continue;
            }
            for(ClientSession session : client.getValidSessions()) {
                if(session.isChoked()) continue;
                boolean[] receivedBitmap = session.getBitmap();
                if(receivedBitmap == null || !receivedBitmap[i]) continue;
                
                int pieceLen = torrentFile.getPieceLength();
                long fileLen = torrentFile.getFileLength();
                int dataLength = (int) ( i == pieceAmount - 1 ? Math.min(fileLen - i * (long) pieceLen, pieceLen) : pieceLen);

                byte[] sessionId = session.getPeerId();
                try {
                    messageSender.send(new Envelope(new Request(i, 0, dataLength), sessionId));
                } catch (IllegalMessageTypeException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        boolean isFileComplete = false;
        try {
            isFileComplete = checkFileWholeness(torrentFile, file);
        } catch (NoSuchAlgorithmException | IOException e) { }

        for(ClientSession session : client.getValidSessions()) {
            try {
                messageSender.send(new Envelope(new NotInterested(), session.getPeerId()));
            } catch (IllegalMessageTypeException e) {
                System.out.println(e.getMessage());
            }
        }

        if(isFileComplete) {
            System.out.println("file completly downloaded");
        } else {
            System.out.println("something went wrong during downloading, restart client");
        }

        executor.close();
    }

    private static byte[] getPeerId() {
        String prefix = "-JT0001-";
        String random = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        String peerIdStr = prefix + random;
        return peerIdStr.getBytes(StandardCharsets.UTF_8);
    }

    private static boolean checkFileWholeness(TorrentFileManager torrentFile, FileManager file) throws NoSuchAlgorithmException, IOException {
        long fileLen = torrentFile.getFileLength();
        int pieceLen = torrentFile.getPieceLength();
        int pieceAmount = torrentFile.getPieceAmount();
        for(int i = 0; i < pieceAmount; i++) {
            int dataLength = (int) ( i == pieceAmount - 1 ? Math.min(fileLen - i * (long) pieceLen, pieceLen) : pieceLen);
            if(!torrentFile.comaparePieceHash(i, file.read(i, 0, dataLength))) 
                return false;
        }
        return true;
    }
}
