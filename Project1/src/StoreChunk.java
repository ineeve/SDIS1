import utils.ThreadUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

public class StoreChunk implements Runnable {

	private static final String CRLF = "\r\n";
	
	private MulticastSocket mdbSocket;
	private Config config;

	private String fileId;
	private int chunkNo;
	private byte replicationDegree;
	private byte[] data;

	private ReplicationStatus repStatus;

	public StoreChunk(Config config, MulticastSocket mdbSocket, String fileId, int chunkNo, byte replicationDegree,
			byte[] data, ReplicationStatus repStatus) {
		this.mdbSocket = mdbSocket;
		this.config = config;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		this.data = data;
		this.repStatus = repStatus;
	}

	@Override
	public void run() {
		repStatus.putchunk_setDesiredReplicationDeg(replicationDegree,fileId,chunkNo);
		storeChunk();
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
					System.out.println(String.format("Sending PUTCHUNK %d", chunkNo));
					mdbSocket.send(chunkPacket);
					chunkSent = true;
				}catch(IOException e){
					//buffer is full
					ThreadUtils.waitBetween(10,400);
				}
			}
			
			ThreadUtils.waitFixed(listeningInterval);
			
			numConfirmations = repStatus.getNumConfirms(fileId, chunkNo);
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
		String putChunkMsgStr = "PUTCHUNK 1.0 " + config.getPeerId() + " " + fileId + " " + chunkNo + " " + repDeg + " " + CRLF + CRLF;
		byte[] putChunkMsgHeader = putChunkMsgStr.getBytes(Charset.forName("ISO_8859_1"));
		byte[] putChunkMsg = new byte[putChunkMsgHeader.length + data.length];
		for (int i = 0; i < putChunkMsg.length; i++) {
			if (i < putChunkMsgHeader.length) {
				putChunkMsg[i] = putChunkMsgHeader[i];
			} else {
				putChunkMsg[i] = data[i - putChunkMsgHeader.length];
			}
		}
		DatagramPacket packet = new DatagramPacket(putChunkMsg, putChunkMsg.length, config.getMdbIP(), config.getMdbPort());
		return packet;
	}
}
