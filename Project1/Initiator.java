import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Initiator implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();
	
	private Scanner terminal = new Scanner(System.in);
	
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;

	private Config config;

	// SHARED
	private final ReplicationStatus replicationStatus;
	private final ChunksRequested chunksRequested = new ChunksRequested();
	
	
	public Initiator(Config config, ReplicationStatus backupStatus) {
		this.config = config;
		this.replicationStatus = backupStatus;
		try {
			mcSocket = new MulticastSocket(config.getMcPort());
			mcSocket.joinGroup(config.getMcIP());
			mcSocket.setTimeToLive(3);
			mdbSocket = new MulticastSocket(config.getMdbPort());
			mdbSocket.joinGroup(config.getMdbIP());
			mdbSocket.setTimeToLive(3);
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
		case 2:
			restoreChunkMenu();
		default:
			return;
		}
	}
	
	private void restoreChunkMenu() {
		System.out.println("File ID: ");
		String fileId = terminal.next();
		pool.execute(new SendRestoreFile(config, mcSocket,fileId, chunksRequested));
	}

	private byte readDesiredReplicationDegree(){
		byte replicationDegree = 0;
		do {
			System.out.println("Desired Replication Degree: ");
			replicationDegree = terminal.nextByte();
		} while(replicationDegree < 1);
		return replicationDegree;
	}

	private void backupChunkMenu() throws IOException {
		File file;
		Path path = null;
		FileInputStream fis = null;
		boolean fileFound;
		byte replicationDegree = 0;

		//get filename and make sure it exists
		do {
			fileFound = true;
			System.out.println("Filename: ");
			String filename = terminal.next();
			file = new File(filename);
			path = Paths.get(filename);
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.out.println("File '" + filename + "' wasn't found, try again.");
				fileFound = false;
			}
		} while (!fileFound);

		//get desired replication degree
		replicationDegree = readDesiredReplicationDegree();

		byte[] data = Files.readAllBytes(path);
		//fis.read(data);
		fis.close();
		String fileId = getFileId(file); // Missing metadata
		pool.execute(new StoreFile(config, mdbSocket, fileId, replicationDegree, data, replicationStatus));
	}

}