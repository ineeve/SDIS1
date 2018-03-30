import java.io.File;
import java.io.IOException;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Initiator implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();
	
//	private Scanner terminal = new Scanner(System.in);
	
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

	private void createWatcher() {
		pool.execute(new WatchStored(config,mcSocket));
	}


	@Override
	public void run() {
		createWatcher();
//		System.out.println("1. Backup file");
//		System.out.println("2. Restore file");
//		System.out.println("3. Delete file");
//        System.out.println("4. Reclaim disk space");
//		System.out.print("\nOption: ");
//		while (true) {
//			int option = terminal.nextInt();
//			treatOption(option);
//		}
	}

/*	private void treatOption(int option) {
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
			break;
		case 3:
			deleteFileMenu();
			break;
		case 4:
            reclaimSpaceMenu();
		default:
			return;
		}
	}

	private void reclaimSpaceMenu(){
        long reservedSpace = -1;
        do {
            System.out.println("Reclaim space (bytes): ");
            try{
                reservedSpace = terminal.nextLong();
            }catch(Exception e){
                System.out.println("Insert positive long integer");
            }
        } while(reservedSpace < 0);
        replicationStatus.setBytesReserved(reservedSpace);
        pool.execute(new ReclaimDiskSpace(config, replicationStatus));
    }
*/

	
//	private void deleteFileMenu() {
//		File file = FileProcessor.loadFileFromTerminal();
//		pool.execute(new SendDeleteFile(config, mcSocket, file));
//	}
//
//
//	private void restoreFileMenu() {
//		File file = FileProcessor.loadFileFromTerminal();
//		pool.execute(new SendRestoreFile(config, mcSocket, file, chunksRequested));
//	}
//
//	private byte readDesiredReplicationDegree(){
//		byte replicationDegree = 0;
//		do {
//			System.out.println("Desired Replication Degree: ");
//			replicationDegree = terminal.nextByte();
//		} while(replicationDegree < 1);
//		return replicationDegree;
//	}
//
//	private void backupFileMenu() throws IOException {
//		FileProcessor fileProcessor = new FileProcessor();
//		File file = fileProcessor.loadFileFromTerminal();
//        byte[] data = FileProcessor.getData(file);
//        if (data != null){
//            byte replicationDegree = 0;
//
//            //get desired replication degree
//            replicationDegree = readDesiredReplicationDegree();
//
//
//            String fileId = FileProcessor.getFileId(file); // Missing metadata
//            pool.execute(new StoreFile(config, mdbSocket, fileId, replicationDegree, data, replicationStatus));
//        }
//	}

//	@Override
//	public void backup(String pathname, byte desiredRepDegree) throws RemoteException {
//		File file = FileProcessor.loadFile(pathname);
//		String fileId = FileProcessor.getFileId(file);
//		byte[] data = FileProcessor.getData(file);
//        pool.execute(new StoreFile(config, mdbSocket, fileId, desiredRepDegree, data, replicationStatus));
//	}
//
//	@Override
//	public void restore(String pathname) throws RemoteException {
//		File file = FileProcessor.loadFile(pathname);
//		pool.execute(new SendRestoreFile(config, mcSocket, file, chunksRequested));
//	}
//
//	@Override
//	public void delete(String pathname) throws RemoteException {
//		File file = FileProcessor.loadFile(pathname);
//		pool.execute(new SendDeleteFile(config, mcSocket, file));
//	}
//
//	@Override
//	public void state() throws RemoteException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void reclaim(int maxDiskSpace) throws RemoteException {
//		// TODO Auto-generated method stub
//		
//	}

}