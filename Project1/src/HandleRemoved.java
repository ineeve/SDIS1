import utils.ThreadUtils;

import java.io.File;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

public class HandleRemoved implements Runnable {

    private MulticastSocket mdbSocket;
    private Config config;
    private ReplicationStatus replicationStatus;
    private DatagramPacket removedPacket;
    private int chunkNo;
    private String fileId;
    private String senderId;

    public HandleRemoved(Config config, ReplicationStatus repStatus, DatagramPacket removedPacket, MulticastSocket mdbSocket){
        this.config = config;
        replicationStatus = repStatus;
        this.removedPacket = removedPacket;
        this.mdbSocket = mdbSocket;
    }
    @Override
    public void run() {
        System.out.println("Handling removed");
        parsePacket();
        if (senderId.equals(config.getPeerId())) return;
        updateReplicationStatus();
        Integer numConfirmations = replicationStatus.getNumConfirms(fileId, chunkNo);
        Byte desiredRepDeg = replicationStatus.getDesiredReplicationDeg(fileId, chunkNo);
        if (numConfirmations < desiredRepDeg){
            waitAndBackup();
        }
    }

    private void parsePacket(){
        String msg = new String(removedPacket.getData(), Charset.forName("ISO_8859_1")).trim();
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
        ThreadUtils.waitBetween(0, 400);
        Integer numConfirmations = replicationStatus.getNumConfirms(fileId, chunkNo);
        byte desiredRepDeg =  replicationStatus.getDesiredReplicationDeg(fileId, chunkNo);
        // We check again to make sure that a PUTCHUNK message for the same chunk was not received meanwhile
        if (numConfirmations < desiredRepDeg){
            String chunkName = FileProcessor.createChunkName(fileId,chunkNo);
            File chunkFile = FileProcessor.loadFile(config.getPeerDir() + "stored/"+ chunkName);
            if (chunkFile != null){
                byte[] data = FileProcessor.getData(chunkFile);
                if (data != null){
                    Thread storeChunkThread = new Thread(new StoreChunk(config, mdbSocket,fileId,chunkNo,desiredRepDeg,data,replicationStatus));
                    storeChunkThread.start();
                }
            }
        }else{
            System.out.println("HandleRemoved: Putchunk received meanwhile");
        }

    }
}