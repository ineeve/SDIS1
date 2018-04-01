import utils.FutureBuffer;

import java.io.File;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.*;

public class StoreFile implements Runnable {

	private ExecutorService pool;
	
	private MulticastSocket mdbSocket;
	private Config config;

	private File file;
	private byte replicationDegree;
	private byte[] fileData;

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
	    /*FutureBuffer futureBuffer = FileProcessor.getDataAsync(file);
		fileData = FileProcessor.getDataFromFuture(futureBuffer);
		ArrayList<byte[]> filePortions = splitFile();
		sendChunks(filePortions);*/

        ArrayList<FutureBuffer> futures = FileProcessor.readFileChunksAsync(file);
        sendChunks(futures);
	}

	private boolean filePortionIsFullSize(int i) {
		return fileData.length - i >= Config.MAX_CHUNK_SIZE;
	}

	private ArrayList<byte[]> splitFile() {
		ArrayList<byte[]> result = new ArrayList<>();
		int i;
		for (i = 0; filePortionIsFullSize(i); i += Config.MAX_CHUNK_SIZE) {
			byte[] filePortion = new byte[Config.MAX_CHUNK_SIZE];
			System.arraycopy(fileData, i, filePortion, 0, Config.MAX_CHUNK_SIZE);
			result.add(filePortion);
		}
		// i now has index of start position of last file portion
		int lastChunkLength = fileData.length - i;
		byte[] lastFilePortion = new byte[lastChunkLength];
		System.arraycopy(fileData, i, lastFilePortion, 0, lastChunkLength);
		result.add(lastFilePortion);
		return result;
	}

	/*private void sendChunks(ArrayList<byte[]> filePortions) {
        String fileId = FileProcessor.getFileId(file);
	    System.out.println("Sending file to store: " + fileId);
		int chunkNo = 0;
		for (byte[] filePortion : filePortions) {
			pool.execute(new StoreChunk(config, mdbSocket, fileId, chunkNo, replicationDegree, repStatus, filePortion));
			chunkNo++;
		}
		repStatus.setNumChunks(fileId, chunkNo);
	}*/

	private void sendChunks(ArrayList<FutureBuffer> filePortions) {
        String fileId = FileProcessor.getFileId(file);
	    System.out.println("Sending file to store: " + fileId);
		int chunkNo = 0;
		for (FutureBuffer filePortion : filePortions) {
			pool.execute(new StoreChunk(config, mdbSocket, fileId, chunkNo, replicationDegree, repStatus, filePortion));
			chunkNo++;
		}
		repStatus.setNumChunks(fileId, chunkNo);
	}

}
