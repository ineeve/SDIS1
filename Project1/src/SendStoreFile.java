import utils.FutureBuffer;

import java.io.File;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class SendStoreFile implements Runnable {

	private ExecutorService pool;
	
	private MulticastSocket mdbSocket;
	private String version;

	private File file;
	private byte replicationDegree;

	private ReplicationStatus repStatus;

	
	public SendStoreFile(MulticastSocket mdbSocket, String version, File file,
						 byte replicationDegree, ReplicationStatus repStatus) {
		this.mdbSocket = mdbSocket;
		this.version = version;
		this.file = file;
		this.replicationDegree = replicationDegree;
		this.repStatus = repStatus;
        pool = Executors.newCachedThreadPool();
    }

	@Override
	public void run() {
        System.out.println("File length: " + file.length());
        ArrayList<FutureBuffer> futures = FileProcessor.readFileChunksAsync(file);
        sendChunks(futures);
	}

	private void sendChunks(ArrayList<FutureBuffer> filePortions) {
        String fileId = FileProcessor.getFileId(file);
	    System.out.println("Sending file to store: " + fileId);
		int chunkNo = 0;
		for (FutureBuffer filePortion : filePortions) {
			pool.execute(new StoreChunk(mdbSocket, version, fileId, chunkNo, replicationDegree, repStatus, filePortion));
			chunkNo++;
		}
		repStatus.setNumChunks(fileId, chunkNo);
	}

}
