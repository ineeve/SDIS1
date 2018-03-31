import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ChunkReceive implements Runnable{

    private static final String CRLF = "\r\n";

    private FilesRestored filesRestored;
    private ChunksRequested chunksRequested; //hashmap of <file,chunks> requested to restore;
    private DatagramPacket chunkPacket;
    private String fileId;
    private Integer chunkNo;
    private Config config;

    public ChunkReceive(DatagramPacket chunkPacket, FilesRestored filesRestored, Config config, ChunksRequested chunksRequested){
        this.filesRestored = filesRestored;
        this.chunkPacket = chunkPacket;
        this.chunksRequested = chunksRequested;
        this.config = config;
    }
	@Override
	public void run() {
        if (parseReceivedChunk()){
            if (haveAll() && !filesRestored.wasFileCreated(fileId) && filesRestored.exists(fileId)){
                createFile();
            }
        }
    }
    
    private boolean parseReceivedChunk(){
        String msg = new String(chunkPacket.getData(), Charset.forName("ISO_8859_1")).trim();
		String crlf = new String(CRLF);
		String[] splitMessage = msg.trim().split(crlf + crlf);
        String head[] = splitMessage[0].split("\\s+");
        String senderId = head[2];
        if (senderId.equals(config.getPeerId())) return false;
		fileId = head[3];
        chunkNo = Integer.parseInt(head[4]);
        if (!chunksRequested.wasRequested(fileId,chunkNo)) return false;
        if (filesRestored.containsChunk(fileId, chunkNo)) return false;
        String body = splitMessage[1];
        filesRestored.addChunk(fileId, chunkNo, body.getBytes(Charset.forName("ISO_8859_1")));
        if (chunkPacket.getLength() < Config.MAX_CHUNK_SIZE){
            filesRestored.setReceivedLastChunk(fileId);
        }
        return true;
    }

    private boolean haveAll(){
        return filesRestored.haveAll(fileId);
    }

    private void createFile() {
        filesRestored.setFileCreated(fileId,true);
        Path outputPath = Paths.get(config.getPeerDir() + "restored/" + fileId);
        ArrayList<byte[]> fileChunks = (ArrayList<byte[]>) filesRestored.getFile(fileId).clone();
        if (fileChunks.size() > 0){
            boolean result = FileProcessor.writeFileAsync(outputPath,fileChunks,Config.MAX_CHUNK_SIZE);
            if (result){
                chunksRequested.clear(fileId);
                filesRestored.removeFile(fileId);
                System.out.println("File being restored to " + outputPath);
            }
        }
    }
    
}