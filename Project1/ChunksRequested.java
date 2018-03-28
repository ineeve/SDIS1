import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

// Used to make sure that a Chunk received in MDR socket was request by the current peer
public class ChunksRequested{
    // maps fileId to chunks that have been requested using the restore protocol
    private ConcurrentHashMap<String, HashSet<Integer>> chunksRequested;

    public ChunksRequested(){
        chunksRequested = new ConcurrentHashMap<String,HashSet<Integer>>();
    }

    public void add(String fileId, int chunkNo){
        chunksRequested.putIfAbsent(fileId, new HashSet<Integer>());
        chunksRequested.get(fileId).add(chunkNo);
    }

    public boolean wasRequested(String fileId, int chunkNo){
        HashSet<Integer> chunksSet = chunksRequested.get(fileId);
        if (chunksSet == null) return false;
        return chunksSet.contains(chunkNo);
    }
    public void clear(String fileId){
        chunksRequested.remove(fileId);
    }
}