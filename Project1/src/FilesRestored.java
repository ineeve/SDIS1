import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

public class FilesRestored {

     // <fileId, <chunkNo, chunk data>>
     private ConcurrentHashMap<String,ConcurrentHashMap<Integer,byte[]>> filesRestored;

    public FilesRestored(){
        filesRestored = new ConcurrentHashMap<String,ConcurrentHashMap<Integer,byte[]>>();
    }
    public void addChunk(String fileId, int chunkNo, byte[] data){
        filesRestored.putIfAbsent(fileId, new ConcurrentHashMap<Integer,byte[]>());
        filesRestored.get(fileId).putIfAbsent(chunkNo, data);
    }
    public boolean containsChunk(String fileId, int chunkNo){
        ConcurrentHashMap<Integer,byte[]> chunks = filesRestored.get(fileId);
        if (chunks == null) return false;
        return chunks.containsKey(chunkNo);
    }
    public ArrayList<byte[]> getFile(String fileId){
        ArrayList<byte[]> fileChunks = new ArrayList<>();
        ConcurrentHashMap<Integer,byte[]> hashmap = filesRestored.get(fileId);
        fileChunks.addAll(hashmap.values());
        return fileChunks;
    }
    public boolean haveAll(String fileId, int lastChunk){
        ConcurrentHashMap<Integer,byte[]> chunks = filesRestored.get(fileId);
        for (int i = 0; i <= lastChunk; i++){
            if (!chunks.containsKey(i)){
                return false;
            }
        }
        return true;
    }
}