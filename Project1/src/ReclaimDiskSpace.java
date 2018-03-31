import utils.Pair;
import java.util.HashSet;
import java.util.Map;

public class ReclaimDiskSpace implements Runnable {

    private Config config;
    private ReplicationStatus replicationStatus;

    public ReclaimDiskSpace(Config config, ReplicationStatus replicationStatus){
        this.config = config;
        this.replicationStatus = replicationStatus;
    }

    @Override
    public void run() {
        System.out.println("Reclaim disk space executing");
        long usedDiskSpace = replicationStatus.getBytesUsed();
        long reservedDiskSpace = replicationStatus.getBytesReserved();
        System.out.println("Using " + usedDiskSpace + " out of " + reservedDiskSpace);
        if (usedDiskSpace > reservedDiskSpace){
            reclaimSpaceAlgorithm();
        }
        System.out.println("Space reclaimed, using " + replicationStatus.getBytesUsed() + " out of " + replicationStatus.getBytesReserved());
    }

    private void reclaimSpaceAlgorithm(){
        //Order chunks by bigger to lower |rep degree - desired|
        Map<Pair<String,Integer>, Pair<Byte, HashSet<String>>> sortedMap = replicationStatus.getSortedMap();
        for(Map.Entry<Pair<String,Integer>, Pair<Byte, HashSet<String>>> entry : sortedMap.entrySet()) {
            //key is <fileId,chunkNo>
            //value is <Desired replication degree, List of peers that are storing the chunk>
            Pair<String,Integer> key = entry.getKey();
            Pair<Byte, HashSet<String>> value = entry.getValue();
            System.out.println("ReclaimDiskSpace: Removing file with " + value.getLeft() + "/" + value.getRight().size());
            String filePath = config.getPeerDir() + "stored/" + FileProcessor.createChunkName(key.getLeft(), key.getRight());
            long fileLength = FileProcessor.loadFile(filePath).length();
            replicationStatus.decrementBytesUsed(fileLength);
            FileProcessor.deleteFile(filePath);
            if (replicationStatus.getBytesUsed() < replicationStatus.getBytesReserved()) break;
        }
    }
}