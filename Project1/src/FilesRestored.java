import utils.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

public class FilesRestored {

     // <fileId, <chunkNo, chunk data>>
     private ConcurrentHashMap<String,ConcurrentHashMap<Integer,byte[]>> filesRestored;

     // <fileId, <wasLastChunkReceived, wasFileRestored>>
     private ConcurrentHashMap<String, Pair<Boolean,Boolean>> filesInfo;


    public FilesRestored(){
        filesRestored = new ConcurrentHashMap<>();
        filesInfo = new ConcurrentHashMap<>();
    }
    public void addChunk(String fileId, int chunkNo, byte[] data){
        System.out.println("FilesRestored: Restored chunk " + chunkNo);
        filesRestored.putIfAbsent(fileId, new ConcurrentHashMap<>());
        filesInfo.putIfAbsent(fileId, new Pair<>(false,false));
        filesRestored.get(fileId).putIfAbsent(chunkNo, data);
    }

    public void setFileCreated(String fileId){
        filesInfo.get(fileId).setRight(true);
    }

    public boolean wasFileCreated(String fileId){
        return filesInfo.get(fileId).getRight();
    }

    public void setReceivedLastChunk(String fileId){

        filesInfo.get(fileId).setLeft(true);
    }
    public boolean wasLastChunkReceived(String fileId){
        return filesInfo.get(fileId).getLeft();
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
    public boolean haveAll(String fileId){
        if(wasLastChunkReceived(fileId)){
            ConcurrentHashMap<Integer,byte[]> chunks = filesRestored.get(fileId);
            for (int i = 0; i < chunks.size(); i++){
                if (!chunks.containsKey(i)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}