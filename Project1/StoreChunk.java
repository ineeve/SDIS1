import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.stream.Stream;

public class StoreChunk implements Runnable {

	private static final String CRLF = "\r\n";
	
	private MulticastSocket mdbSocket;
	private InetAddress mdbIP;
	private int mdbPort;
	private String peerId;

	private String fileId;
	private int chunkNo;
	private byte replicationDegree;
	private byte[] data;

	private BackupStatus backupStatus;

	public StoreChunk(MulticastSocket mdbSocket, InetAddress mdbIP, int mdbPort, String peerId, String fileId, int chunkNo, byte replicationDegree,
			byte[] data, BackupStatus backupStatus) {
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
	private DatagramPacket makeChunkPacket(String fileId, int chunkNo, byte repDeg, byte[] data) {
		String putChunkMsgStr = "PUTCHUNK 1.0 " + peerId + " " + fileId + " " + chunkNo + " " + repDeg + " " + CRLF + CRLF;
		byte[] putChunkMsgHeader = putChunkMsgStr.getBytes(Charset.forName("ISO_8859_1"));
		byte[] putChunkMsg = new byte[putChunkMsgHeader.length + data.length];
		for (int i = 0; i < putChunkMsg.length; i++) {
			if (i < putChunkMsgHeader.length) {
				putChunkMsg[i] = putChunkMsgHeader[i];
			} else {
				putChunkMsg[i] = data[i - putChunkMsgHeader.length];
			}
		}
		DatagramPacket packet = new DatagramPacket(putChunkMsg, putChunkMsg.length, mdbIP, mdbPort);
		return packet;
	}
}
