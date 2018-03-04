import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Initiator implements Runnable {

	private static final byte[] CRLF = {0xD, 0xA};

	private Scanner terminal = new Scanner(System.in);
	
	private InetAddress mdbIP;
	private int mdbPort;
	private InetAddress mcIP;
	private int mcPort;
	
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private String peerId;
	
	ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public Initiator(String peerId, InetAddress mcIP, int mcPort, InetAddress mdbIP, int mdbPort) {
		this.peerId = peerId;
		this.mdbIP = mdbIP;
		this.mdbPort = mdbPort;
		this.mcIP = mcIP;
		this.mcPort = mcPort;
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
	
	private DatagramPacket makeChunkPacket(String fileId, String chunkNo, byte repDeg, String data) {
		String putChunkMsg = "PUTCHUNK 1.0 " + peerId + " " + fileId + " " + chunkNo + " " + repDeg + CRLF + CRLF;
		putChunkMsg += data;
		DatagramPacket packet = new DatagramPacket(putChunkMsg.getBytes(), putChunkMsg.length(), mdbIP, mdbPort);
		return packet;
	}
	
	private void storeChunk(String fileId, String chunkNo, byte repDeg, String data) throws IOException {
		HashSet<String> confirmedPeerIds = new HashSet<String>();
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, repDeg, data);
        DatagramPacket confirmationPacket = new DatagramPacket(new byte[1024], 1024);
		int listeningInterval = 1; //seconds
		for (int i = 1; i <= 5; i++) {
			mdbSocket.send(chunkPacket);
			int remainingListeningTime = listeningInterval * 1000;
			int numConfirmations = 0;
			while (remainingListeningTime > 0) {
				int msBeforeReceive = Calendar.getInstance().get(Calendar.MILLISECOND);
				mcSocket.setSoTimeout(remainingListeningTime); // TIMEOUT is throwing exception
				mcSocket.receive(confirmationPacket);
				System.out.println("Received packet on MC");
				String confirmedPeerId = getPeerIdFromConfirmation(confirmationPacket, fileId, chunkNo);
				if (confirmedPeerId != null && !confirmedPeerIds.contains(confirmedPeerId)) {
					System.out.println("Confirmation received");
					confirmedPeerIds.add(confirmedPeerId);
					++numConfirmations;
				}
				int msAfterReceive = Calendar.getInstance().get(Calendar.MILLISECOND);
				int passedTime = msAfterReceive - msBeforeReceive;
				remainingListeningTime -= passedTime;
			}
			if (numConfirmations >= repDeg) {
				// Missing storage of numConfirmations.
				break;
			}
			listeningInterval *= 2;
		}
	}

	private String encode(String str){
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(hash);
	}
	
	private String getPeerIdFromConfirmation(DatagramPacket confirmationPacket, String sentFileId, String sentChunkNo) {
		String confirmationMsg = new String(confirmationPacket.getData());
		String[] splittedMsg = confirmationMsg.trim().split("\\s+");
		String msgType = splittedMsg[0];
		System.out.println();
		if (!msgType.equals("STORED")) {
			return null;
		}
		
		String peerId = splittedMsg[2];
		String fileId = splittedMsg[3];
		String chunkNo = splittedMsg[4];
		if (!fileId.equals(sentFileId) || !chunkNo.equals(sentChunkNo)) {
			System.out.println("File S-R:" + sentFileId + "-" + fileId);
			System.out.println("Chunk S-R:" + sentChunkNo + "-" + chunkNo);
			return null;
		}
		
		return peerId;
	}

	@Override
	public void run() {
		System.out.println("1. Backup chunk\n");
		System.out.println("Option: ");
		while (true) {
			int option = terminal.nextInt();
			treatOption(option);
		}
	}

	private void treatOption(int option) {
		switch (option) {
		case 1:
			try {
				backupChunkMenu();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		default:
			return;
		}
	}

	private void backupChunkMenu() throws IOException {
		File file;
		FileInputStream fis = null;
		boolean fileFound;
		do {
			fileFound = true;
			System.out.println("Chunk filename: ");
			String filename = terminal.next();
			file = new File(filename);
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.out.println("File '" + filename + "' wasn't found, try again.");
				fileFound = false;
			}
		} while (!fileFound);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		String fileId = encode(file.getName());//generateFileId();
		String chunkNo = "0";
		storeChunk(fileId, chunkNo, (byte) 1, str);
	}

}