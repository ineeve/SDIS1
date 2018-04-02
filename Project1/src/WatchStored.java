import utils.ThreadUtils;
import java.io.IOException;
import java.net.MulticastSocket;
import java.nio.file.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchStored implements Runnable{


    private WatchService watcher;

    private MulticastSocket mcSocket;
    private Set<String> filesToNotWatch;
    private ChunksStored chunksStored;

    private ExecutorService pool = Executors.newCachedThreadPool();

    public WatchStored(Set<String> filesToNotWatch, MulticastSocket mcSocket, ChunksStored chunksStored) {
        this.mcSocket = mcSocket;
        this.filesToNotWatch = filesToNotWatch;
        this.chunksStored = chunksStored;
    }

    @Override
    public void run() {
        Path backupDirectory = Paths.get(Config.getPeerDir() + "stored/");
        WatchKey eventKey;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            backupDirectory.register(watcher, ENTRY_DELETE);
        } catch (IOException x) {
            System.err.println(x);
            return;
        }

        while(true) {
            eventKey = watcher.poll();
            if (eventKey == null){
                ThreadUtils.waitFixed(1000);
                continue;
            }
            for(WatchEvent<?> event: eventKey.pollEvents()){
                boolean stop = false;
                WatchEvent.Kind kind = event.kind();
                if (kind == OVERFLOW) continue;
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path deletedFilePath = ev.context();
                String deletedFileName = deletedFilePath.getFileName().toString();
                for (String s: filesToNotWatch) {
                    if (deletedFileName.startsWith(s)){
                        stop = true;
                        break;
                    }
                }
                if(!stop) reclaimSpace(deletedFilePath);
            }
            boolean valid = eventKey.reset();
            if (!valid){
                break;
            }
        }
    }

    private void reclaimSpace(Path filePath){
        String fileId = FileProcessor.getFileIdByPath(filePath.getFileName());
        Integer chunkNo = FileProcessor.getChunkNo(filePath.getFileName());
        chunksStored.removeIfExists(fileId, chunkNo);
        pool.execute(new RemovedSend(mcSocket, fileId,chunkNo));
    }
}
