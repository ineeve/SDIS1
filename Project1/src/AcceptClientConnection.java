import utils.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class AcceptClientConnection implements Runnable {

    private static final String CRLF = "\r\n";

    private ChunksRequested chunksRequested;
    private FilesRestored filesRestored;

    private Socket clientSocket;
    private String fileId;
    private int chunkNo;
    private byte[] packetReceived;

    public AcceptClientConnection(Socket clientSocket, ChunksRequested chunksRequested, FilesRestored filesRestored){
        this.clientSocket = clientSocket;
        this.chunksRequested = chunksRequested;
        this.filesRestored = filesRestored;
    }

    @Override
    public void run() {
        InputStream in = null;
        try {
            in = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytesReceived = new byte[Config.MAX_CHUNK_SIZE+1000];
        int numBytesRead = 0;
        int totalBytesRead = 0;
        ArrayList<Pair<byte[],Integer>> bytesReceivedArray = new ArrayList<>();
        try {
            while((numBytesRead = in.read(bytesReceived)) != -1){
                totalBytesRead += numBytesRead;
                bytesReceivedArray.add(new Pair<byte[], Integer>(bytesReceived, numBytesRead));
                bytesReceived = new byte[Config.MAX_CHUNK_SIZE+1000];
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Total Read: " + totalBytesRead);
        packetReceived = new byte[totalBytesRead];
        int position = 0;
        for(Pair<byte[],Integer> someBytes : bytesReceivedArray){
            System.arraycopy(someBytes.getLeft(),0, packetReceived, position, someBytes.getRight());
            position += someBytes.getRight();
        }


        if(parseReceivedChunk()){
            System.out.println("Chunk " + chunkNo + " parsed successfully");
            if (filesRestored.haveAll(fileId)){
                System.out.println("Have all chunks");
                if (!filesRestored.wasFileCreated(fileId) && filesRestored.exists(fileId)){
                    createFile();
                }
            }
        }

    }

    private boolean parseReceivedChunk(){
        /*String msg = new String(bytesReceived, Charset.forName("ISO_8859_1"));
        String crlf = new String(CRLF);
        String[] splitMessage = msg.split(crlf + crlf);
        String head[] = splitMessage[0].split("\\s+");
        fileId = head[3];
        chunkNo = Integer.parseInt(head[4]);
        if (!chunksRequested.wasRequested(fileId,chunkNo)) return false;
        if (filesRestored.containsChunk(fileId, chunkNo)) return false;
        byte[] body = splitMessage[1].getBytes(Charset.forName("ISO_8859_1"));
        filesRestored.addChunk(fileId, chunkNo, body);

        if (body.length < Config.MAX_CHUNK_SIZE){
            filesRestored.setReceivedLastChunk(fileId);
        }
        return true;*/
        String msg = new String(packetReceived, StandardCharsets.ISO_8859_1);
        String crlf = new String(CRLF);
        String[] splitMessage = msg.split(crlf + crlf);
        String head[] = splitMessage[0].split("\\s+");
        fileId = head[3];
        chunkNo = Integer.parseInt(head[4]);
        if (!chunksRequested.wasRequested(fileId,chunkNo)) return false;
        if (filesRestored.containsChunk(fileId, chunkNo)) return false;
        String body = splitMessage[1];
        System.out.println("Chunk " + chunkNo + " ;Body length: " + body.length());
        filesRestored.addChunk(fileId, chunkNo, body.getBytes(StandardCharsets.ISO_8859_1));
        if (body.length() < Config.MAX_CHUNK_SIZE){
            filesRestored.setReceivedLastChunk(fileId);
        }
        return true;
    }

    private void createFile() {
        filesRestored.setFileCreated(fileId,true);
        Path outputPath = Paths.get(Config.getPeerDir() + "restored/" + fileId);
        ArrayList<byte[]> fileChunks = (ArrayList<byte[]>) filesRestored.getFile(fileId).clone();
        if (fileChunks.size() > 0){
            ArrayList<Future<Integer>> futures = FileProcessor.writeFileAsync(outputPath,fileChunks,Config.MAX_CHUNK_SIZE);
            if (futures != null){
                chunksRequested.clear(fileId);
                filesRestored.removeFile(fileId);
                System.out.println("File being restored to " + outputPath);
            }
        }
    }
}
