import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PutChunkReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket putChunkPacket;
	private MulticastSocket mcSocket;
	private ChunksStoredFutures chunksStoredFutures;
	private ReplicationStatus repStatus;
	private Set<String> filesToNotWatch;

	public PutChunkReceive(DatagramPacket putChunkPacket, ChunksStoredFutures chunksStoredFutures,
                           ReplicationStatus repStatus, MulticastSocket mcSocket, Set<String> filesToNotWatch) {
		this.putChunkPacket = putChunkPacket;
		this.chunksStoredFutures = chunksStoredFutures;
		this.repStatus = repStatus;
		this.mcSocket = mcSocket;
		this.filesToNotWatch = filesToNotWatch;
	}

	@Override
	public void run() {
	    parseReceivedChunk();
	}
	
	private void parseReceivedChunk() {
        String receivedMsg = new String(Arrays.copyOfRange(putChunkPacket.getData(), 0, putChunkPacket.getLength()), StandardCharsets.ISO_8859_1);
		String crlf = new String(CRLF);
		String[] splitMessage = receivedMsg.split(crlf + crlf);
		String head[] = splitMessage[0].split("\\s+");
		String body = splitMessage[1];
		String protocolVersion = head[1];
		String senderId = head[2];
		String fileId = head[3];
		int chunkNo = Integer.parseInt(head[4]);
		byte desiredRepDeg = (byte) Integer.parseInt(head[5]);
		
		if (senderId.equals(Config.getPeerId())) return;
		
		if (Config.isEnhanced() && protocolVersion.equals(Config.ENH_VERSION)) {
			// discard chunk if actual rep degree is already greater or equal to the desired degree
			if (repStatus.getNumConfirms(fileId, chunkNo) >= desiredRepDeg) {
				return;
			}
		} else if (!protocolVersion.equals(Config.ORIG_VERSION)) {
			System.err.println("PutChunkReceive: Protocol Version unknown: " + protocolVersion);
		}
		
		filesToNotWatch.remove(fileId);
		repStatus.putchunk_setDesiredReplicationDeg(desiredRepDeg, fileId, chunkNo);

		if (!repStatus.peerHasChunk(fileId,chunkNo, Config.getPeerId())){
			storeChunk(body,fileId,chunkNo);
		}
		sendConfirmation(makeStoredPacket(protocolVersion,fileId,chunkNo), chunkNo);
	}

	private void sendConfirmation(DatagramPacket storedPacket, int chunkNo) {
		int timeout = (int)(Math.random() * 400);
		try {
			TimeUnit.MILLISECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			mcSocket.send(storedPacket);
			System.out.println("PutChunkReceive: Stored confirmation sent for chunk: " + chunkNo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	private DatagramPacket makeStoredPacket(String version, String fileId, int chunkNo){
		String storedMsg = "STORED " + version + " " + Config.getPeerId() + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
		DatagramPacket packet = new DatagramPacket(storedMsg.getBytes(), storedMsg.length(), Config.getMcIP(), Config.getMcPort());
		return packet;
	}
	
	private void storeChunk(String body, String fileId, int chunkNo) {
	    System.out.println("Chunk " + chunkNo + " ; body length: " + body.length());
		String chunkPath = Config.getPeerDir() + "stored/" + FileProcessor.createChunkName(fileId,chunkNo);
        if (repStatus.getBytesUsed() + body.length() < repStatus.getBytesReserved()){
            repStatus.incrementBytesUsed(body.length());
            Future<Integer> future = FileProcessor.writeSingleChunkAsync(Paths.get(chunkPath), body.getBytes(Charset.forName("ISO_8859_1")));
			chunksStoredFutures.add(fileId, chunkNo, future);
        }else{
            System.out.println("No disk space available: " + repStatus.getBytesUsed() + "/" + repStatus.getBytesReserved());
        }

	}
	
}
