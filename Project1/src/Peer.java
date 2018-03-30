import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer implements RMIInterface {

	private ExecutorService pool = Executors.newCachedThreadPool();

	private MCListener mcListener;
	private MDBListener mdbListener;
	private MDRListener mdrListener;
	
	private MulticastSocket mcSocket;
	private MulticastSocket mdbSocket;
	
	// SHARED
	private ReplicationStatus repStatus;
	private final ChunksRequested chunksRequested;

	private WatchService watcher;
	
	private Config config;
	
	public Peer(String[] args) throws RemoteException {
		this.chunksRequested = new ChunksRequested();
		this.config = parseArgs(args);
		createFolders();
		createSockets();
		repStatus = ReplicationStatusFactory.getNew(config.getPeerDir());

		mcListener = new MCListener(config, repStatus);
		mdbListener = new MDBListener(config, repStatus);
		mdrListener = new MDRListener(config);

		Thread mdbListenerThr = new Thread(mcListener);
		Thread mcListenerThr = new Thread(mdbListener);
		Thread mdrListenerThr = new Thread(mdrListener);
		
		mdbListenerThr.start();
		mcListenerThr.start();
		mdrListenerThr.start();
	}

	private void createSockets() {
		try {
			mcSocket = new MulticastSocket(config.getMcPort());
			mcSocket.joinGroup(config.getMcIP());
			mcSocket.setTimeToLive(3);
			mdbSocket = new MulticastSocket(config.getMdbPort());
			mdbSocket.joinGroup(config.getMdbIP());
			mdbSocket.setTimeToLive(3);
		} catch (IOException e) {
			System.err.println("Peer: Failed to create sockets.");
			e.printStackTrace();
		}
	}

	private void createFolders(){
		new File( config.getPeerDir() + "data/").mkdirs();
		new File(config.getPeerDir() + "restored/").mkdirs();
		new File(config.getPeerDir() + "stored/").mkdirs();
	}
	
	private Config parseArgs(String[] args) {
		if (args.length < 7) {
			System.out.println("Usage:");
			System.out.println("java Peerid MC_ip MC_port MDB_ip MDB_port MDR_ip MDR_port");
			System.exit(1);
		}
		Config config = new Config(args[0],"1.0");
		try {
			config.setMcIP(InetAddress.getByName(args[1]));
			config.setMcPort(Integer.parseInt(args[2]));
			config.setMdbIP(InetAddress.getByName(args[3]));
			config.setMdbPort(Integer.parseInt(args[4]));
			config.setMdrIP(InetAddress.getByName(args[5]));
			config.setMdrPort(Integer.parseInt(args[6]));
		} catch (UnknownHostException e) {
			System.out.println("Invalid IP argument.");
			e.printStackTrace();
			System.exit(1);
		}
		return config;
	}

	public static void main(String[] args) throws RemoteException {
		try { 
	         // Instantiating the implementation class 
	         Peer peer = new Peer(args); 
	    
	         // Exporting the object of implementation class  
	         // (here we are exporting the remote object to the stub) 
	         RMIInterface stub = (RMIInterface) UnicastRemoteObject.exportObject(peer, 0);  
	         
	         // Binding the remote object (stub) in the registry 
	         Registry registry = LocateRegistry.getRegistry(null);
	         
	         registry.rebind(String.format("Peer_%s", peer.config.getPeerId()), stub);
	         System.out.println("Server ready"); 
	      } catch (Exception e) { 
	         System.err.println("Server exception: " + e.toString()); 
	         e.printStackTrace(); 
	      }
	}

	@Override
	public void backup(String pathname, byte desiredRepDegree) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		String fileId = FileProcessor.getFileId(file);
		byte[] data = FileProcessor.getData(file);
        pool.execute(new StoreFile(config, mdbSocket, fileId, desiredRepDegree, data, repStatus));
	}

	@Override
	public void restore(String pathname) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		pool.execute(new SendRestoreFile(config, mcSocket, file, chunksRequested));
	}

	@Override
	public void delete(String pathname) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		pool.execute(new SendDeleteFile(config, mcSocket, file));
	}

	@Override
	public void state() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reclaim(long maxDiskSpace) throws RemoteException {
		repStatus.setBytesReserved(maxDiskSpace * 1000);
		pool.execute(new ReclaimDiskSpace(config, repStatus));
		
	}

}
