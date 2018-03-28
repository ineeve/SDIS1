import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ChunkReceive implements Runnable{

    private static final String CRLF = "\r\n";

    private FilesRestored filesRestored;
    private DatagramPacket chunkPacket;
    private String fileId;

    public ChunkReceive(DatagramPacket chunkPacket, FilesRestored filesRestored){
        this.filesRestored = filesRestored;
        this.chunkPacket = chunkPacket;
    }
	@Override
	public void run() {
        parseReceivedChunk();
        if (isLast()){
            try{
                createFile();
            }catch(IOException e){
                System.out.println(e);
            }
        }
    }
    
    private void parseReceivedChunk(){
        String msg = new String(chunkPacket.getData(), Charset.forName("ISO_8859_1")).trim();
		String crlf = new String(CRLF);
		String[] splittedMessage = msg.trim().split(crlf + crlf);
		String head[] = splittedMessage[0].split("\\s+");
		fileId = head[3];
        Integer chunkNo = Integer.parseInt(head[4]);
        if (filesRestored.containsChunk(fileId, chunkNo)) return;
        String body = splittedMessage[1];
        filesRestored.addChunk(fileId, chunkNo, body.getBytes(Charset.forName("ISO_8859_1")));
    }
    private boolean isLast(){
        System.out.println("Chunk length " + chunkPacket.getLength());
        return chunkPacket.getLength() < 64000; 
    }
    private void createFile() throws IOException{
        ArrayList<byte[]> fileChunks = filesRestored.getFile(fileId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for( byte[] chunk : fileChunks){
            try {
                stream.write(chunk);
            } finally {
				stream.close();
            }
        }
        
    }
}