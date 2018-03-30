import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.file.Path;

public class RemovedSend implements Runnable {

    private static final String CRLF = "\r\n";
    private Config config;
    private MulticastSocket mcSocket;
    private String fileId;
    private Integer chunkNo;

    public RemovedSend(Config config, MulticastSocket mcSocket, Path filename){
        this.config = config;
        this.mcSocket = mcSocket;
        fileId = FileProcessor.getFileIdByPath(filename);
        chunkNo = FileProcessor.getChunkNo(filename);
    }

    @Override
    public void run() {
        DatagramPacket removedPacket = createPacket();
        sendPacket(removedPacket);
    }

    private void sendPacket(DatagramPacket removedPacket){
        try {
            mcSocket.send(removedPacket);
            System.out.println("Sent REMOVED packet");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private DatagramPacket createPacket(){
        String removedMsgStr = String.format("REMOVED %s %s %s %s %s%s", config.getProtocolVersion(), config.getPeerId(), fileId, chunkNo, CRLF, CRLF);
        byte[] removedMsg = removedMsgStr.getBytes(Charset.forName("ISO_8859_1"));
        return new DatagramPacket(removedMsg, removedMsg.length, config.getMcIP(), config.getMcPort());
    }

}
