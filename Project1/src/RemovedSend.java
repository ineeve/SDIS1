import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class RemovedSend implements Runnable {

    private MulticastSocket mcSocket;
    private String fileId;
    private Integer chunkNo;

    public RemovedSend(MulticastSocket mcSocket,String fileId, Integer chunkNo) {
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
        return new DatagramPacket(removedMsg, removedMsg.length, Config.getMcIP(), Config.getMcPort());
    }

}
