import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ChunkReceive implements Runnable{

    private static final String CRLF = "\r\n";

    private FilesRestored filesRestored;
    private DatagramPacket chunkPacket;
    private String fileId;
    private Integer chunkNo;
    private Config config;

    public ChunkReceive(DatagramPacket chunkPacket, FilesRestored filesRestored, Config config){
        this.filesRestored = filesRestored;
        this.chunkPacket = chunkPacket;
        this.config = config;
    }
	@Override
	public void run() {
        if (parseReceivedChunk()){
            if (receivedLastChunk() && haveAll()){
                try{
                    createFile();
                }catch(IOException e){
                    System.out.println(e);
                }
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
        if (filesRestored.containsChunk(fileId, chunkNo)) return false;
        String body = splitMessage[1];
        filesRestored.addChunk(fileId, chunkNo, body.getBytes(Charset.forName("ISO_8859_1")));
        return true;
    }
    private boolean receivedLastChunk(){
        System.out.println("Chunk length " + chunkPacket.getLength());
        if (chunkPacket.getLength() < Config.MAX_CHUNK_SIZE){
            filesRestored.setReceivedLastChunk(fileId);
            return true;
        }
        return filesRestored.wasLastChunkReceived(fileId);
    }

    private boolean haveAll(){
        return filesRestored.haveAll(fileId, chunkNo);
    }

    private void createFile() throws IOException{
        String outputPath = config.getPeerDir() + "restored/" + fileId;
        ArrayList<byte[]> fileChunks = filesRestored.getFile(fileId);
        if (fileChunks.size() > 0){
            FileOutputStream stream = new FileOutputStream(outputPath);
            for( byte[] chunk : fileChunks){
                try{
                    stream.write(chunk);
                }finally{
                    stream.close();
                }
                
            }
            System.out.println("File restored to " + outputPath);
        }
    }
    
}