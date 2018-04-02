import utils.FutureBuffer;
import utils.ThreadUtils;
import java.io.File;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RemovedReceive implements Runnable {

    private MulticastSocket mdbSocket;
    private ReplicationStatus replicationStatus;
    private DatagramPacket removedPacket;
    private int chunkNo;
    private String fileId;
    private String senderId;

    public RemovedReceive(ReplicationStatus repStatus, DatagramPacket removedPacket, MulticastSocket mdbSocket) {
        replicationStatus = repStatus;
        this.removedPacket = removedPacket;
        this.mdbSocket = mdbSocket;
    }
    @Override
    public void run() {
        parsePacket();
        if (senderId.equals(Config.getPeerId())) return;
        updateReplicationStatus();
        Integer numConfirmations = replicationStatus.getNumConfirms(fileId, chunkNo);
        Byte desiredRepDeg = replicationStatus.getDesiredReplicationDeg(fileId, chunkNo);
        if (numConfirmations < desiredRepDeg){
            waitAndBackup();
        }
    }

    private void parsePacket(){
        String msg = new String(Arrays.copyOfRange(removedPacket.getData(), 0, removedPacket.getLength()), StandardCharsets.ISO_8859_1);
        String head[] = msg.split("\\s+");
        senderId = head[2];
        fileId = head[3];
        chunkNo = Integer.parseInt(head[4]);
    }

    /**
     * Decrement the replication status of the chunk by 1;
     */
    private void updateReplicationStatus(){
        replicationStatus.decrementLocalCount(senderId,fileId,chunkNo);
    }

    private void waitAndBackup(){
        System.out.println("Handling removed: " + fileId + "/" + chunkNo);
        ThreadUtils.waitBetween(0, 400);
        Integer numConfirmations = replicationStatus.getNumConfirms(fileId, chunkNo);
        byte desiredRepDeg =  replicationStatus.getDesiredReplicationDeg(fileId, chunkNo);
        // We check again to make sure that a PUTCHUNK message for the same chunk was not received meanwhile
        if (numConfirmations < desiredRepDeg){
            String chunkName = FileProcessor.createChunkName(fileId,chunkNo);
            File chunkFile = FileProcessor.loadFile(Config.getPeerDir() + "stored/"+ chunkName);
            if (chunkFile != null){
                FutureBuffer futureBuffer = FileProcessor.getDataAsync(chunkFile);
                if (futureBuffer != null){
                    Thread storeChunkThread = new Thread(new StoreChunk(mdbSocket,Config.getCurrentVersion(),fileId,chunkNo,
                            desiredRepDeg, replicationStatus, futureBuffer));
                    storeChunkThread.start();
                }
            }
        }else{
            System.out.println("RemovedReceive: Putchunk received meanwhile");
        }

    }
}
