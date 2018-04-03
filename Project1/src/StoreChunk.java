import utils.FutureBuffer;
import utils.ThreadUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class StoreChunk implements Runnable {

	private byte[] body;
    private FutureBuffer future;

    private MulticastSocket mdbSocket;
	private String version;

	private String fileId;
	private int chunkNo;
	private byte replicationDegree;

	private ReplicationStatus repStatus;

	public StoreChunk(Config config, MulticastSocket mdbSocket, String version, String fileId, int chunkNo, byte replicationDegree,
                      ReplicationStatus repStatus, byte[] body) {
		this.mdbSocket = mdbSocket;
		this.version = version;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		this.body = body;
		this.repStatus = repStatus;
	}

	public StoreChunk(MulticastSocket mdbSocket, String version, String fileId, int chunkNo, byte replicationDegree,
					  ReplicationStatus repStatus, FutureBuffer future) {
		this.mdbSocket = mdbSocket;
		this.version = version;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		this.future = future;
		this.repStatus = repStatus;
	}

	@Override
	public void run() {
		storeChunk();
        repStatus.putchunk_setDesiredReplicationDeg(replicationDegree,fileId,chunkNo);
	}

	private void storeChunk() {
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, replicationDegree);
		int listeningInterval = 1000; // milliseconds

		int numConfirmations = 0;
		for (int i = 1; i <= 5; i++) {
            boolean chunkSent = false;
			while(!chunkSent){
				try {
					System.out.format("StoreChunk: Sending PUTCHUNK %d\n", chunkNo);
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
		System.out.format("Stored %s_%d; Rep Degree: %d/%d\n", fileId, chunkNo, numConfirmations, replicationDegree);
	}

	private DatagramPacket makeChunkPacket(String fileId, int chunkNo, byte repDeg) {
		byte[] putChunkMsgHeader = Messages.getPUTCHUNKHeader(version,fileId,chunkNo,repDeg);
		if (body == null){
			body = FileProcessor.getDataFromFuture(future);
		}
		byte[] putChunkMsg = new byte[putChunkMsgHeader.length + body.length];
		System.arraycopy(putChunkMsgHeader, 0, putChunkMsg, 0, putChunkMsgHeader.length);
		System.arraycopy(body,0,putChunkMsg,putChunkMsgHeader.length, body.length);
		DatagramPacket packet = new DatagramPacket(putChunkMsg, putChunkMsg.length, Config.getMdbIP(), Config.getMdbPort());
		return packet;
	}
}
