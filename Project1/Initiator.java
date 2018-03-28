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
	private final ChunksRequested chunksRequested;
	
	
	public Initiator(Config config, ReplicationStatus backupStatus) {
		this.chunksRequested = new ChunksRequested();
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
		FileProcessor fileProcessor = new FileProcessor();
		File file = fileProcessor.loadFileFromTerminal();
		Path path = Paths.get(file.getCanonicalPath());
		byte replicationDegree = 0;

		//get desired replication degree
		replicationDegree = readDesiredReplicationDegree();

		byte[] data = Files.readAllBytes(path);
		String fileId = fileProcessor.getFileId(file); // Missing metadata
		pool.execute(new StoreFile(config, mdbSocket, fileId, replicationDegree, data, replicationStatus));
	}

}