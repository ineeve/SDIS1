import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChunksStored {

    ConcurrentHashMap<String, ConcurrentHashMap<Integer, Future<Integer>>> chunksStored;

    public ChunksStored(){
        chunksStored = new ConcurrentHashMap<>();
    }

    public void add(String fileId, Integer chunkNo, Future<Integer> future){
        chunksStored.putIfAbsent(fileId, new ConcurrentHashMap<>());
        chunksStored.get(fileId).put(chunkNo, future);
    }

    public boolean contains(String fileId, Integer chunkNo){
        ConcurrentHashMap<Integer, Future<Integer>> chunksStoredForFile = chunksStored.get(fileId);
        if (chunksStoredForFile != null){
            return chunksStoredForFile.containsKey(chunkNo);
        }
        return false;
    }

    public void removeIfExists(String fileId, Integer chunkNo){
        chunksStored.remove(fileId, chunkNo);
    }

    public Boolean getFuture(String fileId, Integer chunkNo){
        ConcurrentHashMap<Integer, Future<Integer>> chunksForFile = chunksStored.get(fileId);
        if (chunksForFile != null){
            Future<Integer> future = chunksForFile.get(chunkNo);
            try {
                future.get();
                System.out.println("File " + FileProcessor.createChunkName(fileId,chunkNo) + " processed");
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
