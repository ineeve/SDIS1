import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Initiator implements Runnable {

	private static final String CRLF = "\r\n";

	private ExecutorService pool = Executors.newCachedThreadPool();
	
	private Scanner terminal = new Scanner(System.in);
	
	private InetAddress mdbIP;
	private int mdbPort;
	private InetAddress mcIP;
	private int mcPort;
	
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private String peerId;

	// SHARED
	private final BackupStatus backupStatus;
	
	
	public Initiator(String peerId, InetAddress mcIP, int mcPort, InetAddress mdbIP, int mdbPort, BackupStatus backupStatus) {
		this.peerId = peerId;
		this.mdbIP = mdbIP;
		this.mdbPort = mdbPort;
		this.mcIP = mcIP;
		this.mcPort = mcPort;
		this.backupStatus = backupStatus;
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
	
	private String getFileId(File file){
		String filename = file.getName();
		BasicFileAttributes attr = null;
		try {
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte[] hash = digest.digest((filename + attr.lastModifiedTime()).getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
	    for (byte b : hash) {
	        sb.append(String.format("%02X", b));
	    }
	    return sb.toString();
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
		String fileId = getFileId(file); // Missing metadata
		int chunkNo = 0;
		pool.execute(new StoreChunk(mdbSocket, mdbIP, mdbPort, peerId, fileId, chunkNo, (byte) 1, str, backupStatus));
	}

}