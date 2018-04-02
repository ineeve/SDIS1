import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class RemovedSend implements Runnable {

    private static final String CRLF = "\r\n";
    private Config config;
    private MulticastSocket mcSocket;
    private String fileId;
    private Integer chunkNo;

    public RemovedSend(Config config, MulticastSocket mcSocket,String fileId, Integer chunkNo){
        this.config = config;
        this.mcSocket = mcSocket;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
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
        byte[] removedMsg = Messages.getREMOVEDHeader(fileId, chunkNo);
        return new DatagramPacket(removedMsg, removedMsg.length, config.getMcIP(), config.getMcPort());
    }

}
