import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RESTORE PROTOCOL - LISTENS FOR PACKETS - CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
 */
public class MDRListener implements Runnable{

    private ExecutorService pool = Executors.newCachedThreadPool();

    private MulticastSocket mdrSocket;

    // maps <fileId, <chunkNo, chunk data>>
    private FilesRestored filesRestored;
    private ChunksRequested chunksRequested;

    public MDRListener(ChunksRequested chunksRequested, FilesRestored filesRestored) {
        this.chunksRequested = chunksRequested;
        this.filesRestored = filesRestored;
        try {
			mdrSocket = new MulticastSocket(Config.getMdrPort());
			mdrSocket.joinGroup(Config.getMdrIP());
		} catch (IOException e) {
			System.out.println("Failed to start MDRListener service.");
			e.printStackTrace();
		}
    }

	@Override
	public void run() {
		while(true){
            receiveChunk();
        }
    }
    
    private void receiveChunk(){
        int datagramMaxSize = (int) Math.pow(2,16);
		DatagramPacket chunkPacket = new DatagramPacket(new byte[datagramMaxSize], datagramMaxSize);
		try {
			mdrSocket.receive(chunkPacket);
		} catch (IOException e) {
			e.printStackTrace();
        }
        if (Messages.isChunk(chunkPacket)){
            pool.execute(new ChunkReceive(chunkPacket, filesRestored, chunksRequested));
        }else{
            System.out.println("Caught unhandled message in MDRListener");
        }
    }
}