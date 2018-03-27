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
import java.util.concurrent.ThreadLocalRandom;

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

	private ReplicationStatus backupStatus;

	public StoreChunk(MulticastSocket mdbSocket, InetAddress mdbIP, int mdbPort, String peerId, String fileId, int chunkNo, byte replicationDegree,
			byte[] data, ReplicationStatus backupStatus) {
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
		storeChunk();
	}

	private void sleepThread(int time){
		try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				System.out.println("Thread Interrupted");
				Thread.currentThread().interrupt();
			}
	}

	private void storeChunk() {
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, replicationDegree, data);
		int listeningInterval = 1000; // milliseconds
		boolean success = false;
		boolean chunkSent = false;
		int numConfirmations = 0;
		for (int i = 1; i <= 5; i++) {
			while(!chunkSent){
				try {
					mdbSocket.send(chunkPacket);
					chunkSent = true;
				}catch(IOException e){
					//buffer is full
					sleepThread(ThreadLocalRandom.current().nextInt(10, 400));
				}
			}
			
			sleepThread(listeningInterval);
			
			numConfirmations = backupStatus.getNumConfirms(fileId, chunkNo);
			if (numConfirmations >= replicationDegree) {
				success = true;
				break;
			}
			listeningInterval *= 2;
		}
		if (success) {
			System.out.println(String.format("Stored %s_%d; Rep Degree: %d/%d", fileId, chunkNo, numConfirmations, replicationDegree));
		} else {
			System.out.println(String.format("SMALL REP DEGREE: file ID %s and chunk number %d.", fileId, chunkNo));
		}
	}

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
