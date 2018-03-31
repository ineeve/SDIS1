import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreFile implements Runnable {

	/**
	 * Bytes.
	 */
	private final static int FILE_PORTION_SIZE = 64000;
	
	private ExecutorService pool;
	
	private MulticastSocket mdbSocket;
	private Config config;

	private String fileId;
	private byte replicationDegree;
	private byte[] fileData;

	private ReplicationStatus repStatus;
	
	public StoreFile(Config config, MulticastSocket mdbSocket, String fileId,
			byte replicationDegree, byte[] data, ReplicationStatus repStatus) {
		this.mdbSocket = mdbSocket;
		this.config = config;
		this.fileId = fileId;
		this.replicationDegree = replicationDegree;
		this.fileData = data;
		this.repStatus = repStatus;
        pool = Executors.newCachedThreadPool();
    }

	@Override
	public void run() {
		ArrayList<byte[]> filePortions = splitFile();
		sendChunks(filePortions);
	}

	private ArrayList<byte[]> splitFile() {
		ArrayList<byte[]> result = new ArrayList<byte[]>();
		int i;
		for (i = 0; filePortionIsFullSize(i); i += FILE_PORTION_SIZE) {
			byte[] filePortion = new byte[FILE_PORTION_SIZE];
			System.arraycopy(fileData, i, filePortion, 0, FILE_PORTION_SIZE);
			result.add(filePortion);
		}
		// i now has index of start position of last file portion
		int lastChunkLength = fileData.length - i;
		byte[] lastFilePortion = new byte[lastChunkLength];
		System.arraycopy(fileData, i, lastFilePortion, 0, lastChunkLength);
		result.add(lastFilePortion);
		return result;
	}

	private boolean filePortionIsFullSize(int i) {
		return fileData.length - i >= FILE_PORTION_SIZE;
	}

	private void sendChunks(ArrayList<byte[]> filePortions) {
	    System.out.println("Sending file to store: " + fileId);
		int chunkNo = 0;
		for (byte[] filePortion : filePortions) {
			pool.execute(new StoreChunk(config, mdbSocket, fileId, chunkNo, replicationDegree, filePortion, repStatus));
			chunkNo++;
		}
		repStatus.setNumChunks(fileId, chunkNo);
	}

}
