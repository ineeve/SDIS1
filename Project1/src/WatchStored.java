import java.io.IOException;
import java.net.MulticastSocket;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchStored implements Runnable{

    private WatchService watcher;

    private Config config;
    private MulticastSocket mcSocket;

    private ExecutorService pool = Executors.newCachedThreadPool();

    public WatchStored(Config config, MulticastSocket mcSocket){
        this.config = config;
        this.mcSocket = mcSocket;
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Path backupDirectory = Paths.get(config.getPeerDir() + "stored/");
        WatchKey eventKey;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            backupDirectory.register(watcher, ENTRY_DELETE);
        } catch (IOException x) {
            System.err.println(x);
            return;
        }
        while(true){
            try{
                eventKey = watcher.take();
            }catch(InterruptedException x){
                return;
            }
            for(WatchEvent<?> event: eventKey.pollEvents()){
                WatchEvent.Kind kind = event.kind();
                if (kind == OVERFLOW) continue;
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path deletedFilename = ev.context();
                reclaimSpace(deletedFilename);
            }
            boolean valid = eventKey.reset();
            if (!valid){
                break;
            }
        }
    }

    private void reclaimSpace(Path filepath){
        pool.execute(new RemovedSend(config, mcSocket, filepath));
    }
}
