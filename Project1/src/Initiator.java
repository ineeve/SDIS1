import java.io.File;
import java.io.IOException;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		System.out.println("1. Backup file");
		System.out.println("2. Restore file");
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
				backupFileMenu();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			restoreFileMenu();
		default:
			return;
		}
	}
	
	private void restoreFileMenu() {
		FileProcessor fileProcessor = new FileProcessor();
		File file = fileProcessor.loadFileFromTerminal();
		pool.execute(new SendRestoreFile(config, mcSocket,file, chunksRequested));
	}

	private byte readDesiredReplicationDegree(){
		byte replicationDegree = 0;
		do {
			System.out.println("Desired Replication Degree: ");
			replicationDegree = terminal.nextByte();
		} while(replicationDegree < 1);
		return replicationDegree;
	}

	private void backupFileMenu() throws IOException {
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