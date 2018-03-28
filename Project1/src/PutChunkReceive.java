import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PutChunkReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket putChunkPacket;
	private MulticastSocket mcSocket;
	private ConcurrentHashMap<String, ArrayList<Integer>> chunksStored;
	private ReplicationStatus repStatus;

	private Config config;

	public PutChunkReceive(Config config, DatagramPacket putChunkPacket, ConcurrentHashMap<String,ArrayList<Integer>> chunksStored, ReplicationStatus repStatus, MulticastSocket mcSocket) {
		this.config = config;
		this.putChunkPacket = putChunkPacket;
		this.chunksStored = chunksStored;
		this.repStatus = repStatus;
		this.mcSocket = mcSocket;
	}

	@Override
	public void run() {
		parseReceivedChunk();
	}
	
	private void parseReceivedChunk() {
		String receivedMsg = new String(putChunkPacket.getData(), Charset.forName("ISO_8859_1"));
		String crlf = new String(CRLF);
		String[] splittedMessage = receivedMsg.trim().split(crlf + crlf);
		String head[] = splittedMessage[0].split("\\s+");
		String body = splittedMessage[1];
		String version = head[1];
		String senderId = head[2];
		if (senderId.equals(config.getPeerId())) return;
			String fileId = head[3];
			int chunkNo = Integer.parseInt(head[4]);
			int repDeg = Integer.parseInt(head[5]);
			chunksStored.putIfAbsent(fileId, new ArrayList<Integer>());
			repStatus.putchunk_setDesiredReplicationDeg(repDeg, fileId, chunkNo);
			ArrayList<Integer> chunksStoredForFile = chunksStored.get(fileId);
			try {
				storeChunk(body,fileId,chunkNo);
				if(chunksStoredForFile.contains(chunkNo)){
					System.out.println("chunk already stored");
				}else{
					chunksStoredForFile.add(chunkNo);
				}
				sendConfirmation(makeStoredPacket(version,fileId,chunkNo));
			} catch (IOException e) {
				System.out.format("Failed to store chunk %d of file %s.\n", chunkNo, fileId);
			}
	}

	private void sendConfirmation(DatagramPacket storedPacket) {
		int timeout = (int)(Math.random() * 400);
		try {
			TimeUnit.MILLISECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			mcSocket.send(storedPacket);
			System.out.println("Stored confirmation sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	private DatagramPacket makeStoredPacket(String version, String fileId, int chunkNo){
		String storedMsg = "STORED " + version + " " + config.getPeerId() + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
		DatagramPacket packet = new DatagramPacket(storedMsg.getBytes(), storedMsg.length(), config.getMcIP(), config.getMcPort());
		return packet;
	}
	
	private void storeChunk(String body, String fileId, int chunkNo) throws IOException {
		File chunk = new File("stored/" + fileId + "_" + chunkNo + ".out");
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(chunk)));
		out.writeBytes(body);
		out.close();
	}
	
}
