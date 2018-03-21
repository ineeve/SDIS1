import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;

public class StoreChunk implements Runnable {

	private static final String CRLF = "\r\n";
	
	private MulticastSocket mdbSocket;
	private InetAddress mdbIP;
	private int mdbPort;
	private String peerId;

	private String fileId;
	private int chunkNo;
	private byte replicationDegree;
	private String data;

	private BackupStatus backupStatus;

	public StoreChunk(MulticastSocket mdbSocket, InetAddress mdbIP, int mdbPort, String peerId, String fileId, int chunkNo, byte replicationDegree,
			String data, BackupStatus backupStatus) {
		this.mdbSocket = mdbSocket;
		this.mdbIP = mdbIP;
		this.mdbPort = mdbPort;
		this.peerId = peerId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		this.data = data;
		this.backupStatus = backupStatus;
	}

	@Override
	public void run() {
		try {
			storeChunk();
		} catch (IOException e) {
			System.out.println(
					String.format("Failed to store chunk with file ID %s and chunk number %d.", fileId, chunkNo));
		}
	}

	private void storeChunk() throws IOException {
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, replicationDegree, data);
		int listeningInterval = 1000; // milliseconds
		boolean success = false;
		for (int i = 1; i <= 5; i++) {
			mdbSocket.send(chunkPacket);
			try {
				Thread.sleep(listeningInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
			int numConfirmations = backupStatus.getNumConfirms(fileId, chunkNo);
			if (numConfirmations >= replicationDegree) {
				// Missing storage of numConfirmations.
				success = true;
				break;
			}
			listeningInterval *= 2;
		}
		if (success) {
			System.out.println(String.format("Stored chunk with file ID %s and chunk number %d.", fileId, chunkNo));
		} else {
			System.out.println(String.format("Failed to store chunk with file ID %s and chunk number %d.", fileId, chunkNo));
		}
	}

	// TODO: Use array length instead of string length.
	private DatagramPacket makeChunkPacket(String fileId, int chunkNo, byte repDeg, String data) {
		String putChunkMsg = "PUTCHUNK 1.0 " + peerId + " " + fileId + " " + chunkNo + " " + repDeg + " " + CRLF + CRLF;
		putChunkMsg += data;
		DatagramPacket packet = new DatagramPacket(putChunkMsg.getBytes(), putChunkMsg.length(), mdbIP, mdbPort);
		return packet;
	}
}
