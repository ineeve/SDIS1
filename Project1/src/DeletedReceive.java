import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class DeletedReceive implements Runnable {

    private DatagramPacket packet;
    private ReplicationStatus replicationStatus;
    private String fileId;
    private String senderId;

    public DeletedReceive(DatagramPacket packet, ReplicationStatus replicationStatus){
        this.packet = packet;
        this.replicationStatus = replicationStatus;
    }

    @Override
    public void run() {
        parseReceivedPacket();
        System.out.println("Received confirmation of delete of file");
        replicationStatus.reduceReplicationOfFile(fileId,senderId);
    }
    private void parseReceivedPacket(){
        String msgStr = new String(packet.getData(), StandardCharsets.ISO_8859_1).trim();
        String[] msgSplit = msgStr.split(" ");
        senderId = msgSplit[2];
        fileId = msgSplit[3];
    }
}
