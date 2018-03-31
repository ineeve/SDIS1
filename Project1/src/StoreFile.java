import utils.FutureBuffer;

import java.io.File;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class StoreFile implements Runnable {

	/**
	 * Bytes.
	 */
	private final static int FILE_PORTION_SIZE = 64000;
	
	private ExecutorService pool;
	
	private MulticastSocket mdbSocket;
	private Config config;

	private File file;
	private byte replicationDegree;
	private ArrayList<FutureBuffer> chunksBuffers;

	private ReplicationStatus repStatus;
	
	public StoreFile(Config config, MulticastSocket mdbSocket, File file,
                     byte replicationDegree, ReplicationStatus repStatus) {
		this.mdbSocket = mdbSocket;
		this.config = config;
		this.file = file;
		this.replicationDegree = replicationDegree;
		this.repStatus = repStatus;
        pool = Executors.newCachedThreadPool();
    }

	@Override
	public void run() {
		ArrayList<FutureBuffer> filePortions = splitFile();
		sendChunks(filePortions);
	}

	private ArrayList<FutureBuffer> splitFile() {
        return FileProcessor.readFileChunksAsync(file,Config.MAX_CHUNK_SIZE);
	}

	private void sendChunks(ArrayList<FutureBuffer> filePortions) {
        String fileId = FileProcessor.getFileId(file);
	    System.out.println("Sending file to store: " + fileId);
		int chunkNo = 0;
		for (FutureBuffer filePortion : filePortions) {
			pool.execute(new StoreChunk(config, mdbSocket, fileId, chunkNo, replicationDegree, repStatus, filePortion));
			chunkNo++;
		}
	}

}
