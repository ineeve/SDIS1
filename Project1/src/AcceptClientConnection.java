import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class AcceptClientConnection implements Runnable {

    private static final String CRLF = "\r\n";

    private ChunksRequested chunksRequested;
    private FilesRestored filesRestored;

    private Config config;
    private Socket clientSocket;
    private String fileId;
    private int chunkNo;
    private byte[] bytesReceived;

    public AcceptClientConnection(Config config, Socket clientSocket, ChunksRequested chunksRequested, FilesRestored filesRestored){
        this.clientSocket = clientSocket;
        this.chunksRequested = chunksRequested;
        this.filesRestored = filesRestored;
        this.config = config;
    }

    @Override
    public void run() {
        InputStream in = null;
        try {
            in = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytesReceived = new byte[Config.MAX_CHUNK_SIZE+100];
        try {
            in.read(bytesReceived, 0, bytesReceived.length);
        } catch (IOException e) {
            e.printStackTrace();
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
        String msg = new String(bytesReceived, Charset.forName("ISO_8859_1"));
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
        return true;
    }

    private void createFile() {
        filesRestored.setFileCreated(fileId,true);
        Path outputPath = Paths.get(config.getPeerDir() + "restored/" + fileId);
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
