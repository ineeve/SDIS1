import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreFile implements Runnable {

	/**
	 * Bytes.
	 */
	private final static int FILE_PORTION_SIZE = 64000;
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	private MulticastSocket mdbSocket;
	private InetAddress mdbIP;
	private int mdbPort;
	private String peerId;

	private String fileId;
	private byte replicationDegree;
	private byte[] fileData;

	private ReplicationStatus backupStatus;
	
	public StoreFile(MulticastSocket mdbSocket, InetAddress mdbIP, int mdbPort, String peerId, String fileId,
			byte replicationDegree, byte[] data, ReplicationStatus backupStatus) {
		this.mdbSocket = mdbSocket;
		this.mdbIP = mdbIP;
		this.mdbPort = mdbPort;
		this.peerId = peerId;
		this.fileId = fileId;
		this.replicationDegree = replicationDegree;
		this.fileData = data;
		this.backupStatus = backupStatus;
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
		int chunkNo = 0;
		for (byte[] filePortion : filePortions) {
			pool.execute(new StoreChunk(mdbSocket, mdbIP, mdbPort, peerId, fileId, chunkNo, replicationDegree, filePortion, backupStatus));
			chunkNo++;
		}
	}

}
