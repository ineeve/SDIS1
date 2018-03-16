import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PutChunkReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket putChunkPacket;
	private MulticastSocket mcSocket;
	private InetAddress mcIP;
	private int mcPort;
	private ConcurrentHashMap<String, ArrayList<Integer>> chunksStored;

	private String peerId;

	public PutChunkReceive(DatagramPacket putChunkPacket, String peerId, ConcurrentHashMap<String,ArrayList<Integer>> chunksStored, MulticastSocket mcSocket, InetAddress mcIP, int mcPort) {
		this.putChunkPacket = putChunkPacket;
		this.peerId = peerId;
		this.chunksStored = chunksStored;
		this.mcSocket = mcSocket;
		this.mcIP = mcIP;
		this.mcPort = mcPort;
	}

	@Override
	public void run() {
		parseReceivedChunk();
	}
	
	private void parseReceivedChunk() {
		String receivedMsg = new String(putChunkPacket.getData());
		String crlf = new String(CRLF);
		String[] splittedMessage = receivedMsg.trim().split(crlf + crlf);
		String head[] = splittedMessage[0].split("\\s+");
		String body = splittedMessage[1];
		String version = head[1];
		String senderId = head[2];
		if (senderId.equals(peerId)) return;
		if (head[0].equals("PUTCHUNK")){
			String fileId = head[3];
			int chunkNo = Integer.parseInt(head[4]);
			chunksStored.putIfAbsent(fileId, new ArrayList<Integer>());
			ArrayList<Integer> chunksStoredForFile = chunksStored.get(fileId);
			if(chunksStoredForFile.contains(chunkNo)){
				System.out.println("chunk already stored");
			}else{
				chunksStoredForFile.add(chunkNo);
			}
			storeChunk(body,fileId,chunkNo);
			sendConfirmation(makeStoredPacket(version,fileId,chunkNo));
		}else{
			System.out.println("Received " + head[0]);
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
		String storedMsg = "STORED " + version + " " + peerId + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
		DatagramPacket packet = new DatagramPacket(storedMsg.getBytes(), storedMsg.length(), mcIP, mcPort);
		return packet;
	}
	
	private void storeChunk(String body, String fileId, int chunkNo) {
		File chunk = new File("stored/" + fileId + "_" + chunkNo + ".out");
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(chunk)));
			out.writeBytes(body);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
