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
		storeChunk();
        repStatus.putchunk_setDesiredReplicationDeg(replicationDegree,fileId,chunkNo);
	}

	private void storeChunk() {
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, replicationDegree, data);
		int listeningInterval = 1000; // milliseconds

		int numConfirmations = 0;
		for (int i = 1; i <= 5; i++) {
            boolean chunkSent = false;
			while(!chunkSent){
				try {
					System.out.println(String.format("StoreChunk: Sending PUTCHUNK %d", chunkNo));
					mdbSocket.send(chunkPacket);
					chunkSent = true;
				}catch(IOException e){
					//buffer is full
					ThreadUtils.waitBetween(100,1000);
				}
			}
			
			ThreadUtils.waitFixed(listeningInterval);
			
			numConfirmations = repStatus.getNumConfirms(fileId, chunkNo);
			if (numConfirmations >= replicationDegree) {
				break;
			}
			System.out.println("StoreChunk: Waiting confirmation for chunk " + chunkNo);
			listeningInterval *= 2;
		}
		System.out.println(String.format("Stored %s_%d; Rep Degree: %d/%d", fileId, chunkNo, numConfirmations, replicationDegree));
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
