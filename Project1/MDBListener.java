import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MDBListener implements Runnable {

	private static final byte[] CRLF = {0xD, 0xA};
	private HashMap<String,ArrayList<Integer>> chunksStored; //filename to chunks
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private String peerId;
	private InetAddress mdbIP;
	private int mdbPort;
	private InetAddress mcIP;
	private int mcPort;

	public MDBListener(String peerId, InetAddress mcIP, int mcPort, InetAddress mdbIP, int mdbPort) {
		this.peerId = peerId;
		this.mdbIP = mdbIP;
		this.mdbPort = mdbPort;
		this.mcIP = mcIP;
		this.mcPort = mcPort;
		chunksStored = new HashMap<String, ArrayList<Integer>>();
		try {
			mcSocket = new MulticastSocket(mcPort);
			mcSocket.joinGroup(mcIP);
			mdbSocket = new MulticastSocket(mdbPort);
			mdbSocket.joinGroup(mdbIP);
		} catch (IOException e) {
			System.out.println("Failed to start Initiator service.");
			e.printStackTrace();
		}
	}

	public void receiveChunk(){
		int datagramMaxSize = (int) Math.pow(2,16);
		DatagramPacket putchunkPacket = new DatagramPacket(new byte[datagramMaxSize], datagramMaxSize);
		try {
			mdbSocket.receive(putchunkPacket);
			parseReceivedchunk(putchunkPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseReceivedchunk(DatagramPacket putchunkPacket) {
		String receivedMsg = new String(putchunkPacket.getData());
		String[] splittedMessage = receivedMsg.trim().split("\\r\\n\\r\\n");
		String head[] = splittedMessage[0].split("\\s+");;
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

	@Override
	public void run() {
		receiveChunk();
		
	}

}
