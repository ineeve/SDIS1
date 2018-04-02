import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer implements RMIInterface {

	private int TCP_PORT;
	private ExecutorService pool = Executors.newCachedThreadPool();

	private MCListener mcListener;
	private MDBListener mdbListener;
	private MDRListener mdrListener;
	
	private DeleteWatcher deleteWatcher;
	
	private MulticastSocket mcSocket;
	private MulticastSocket mdbSocket;
	
	// SHARED
	private ReplicationStatus repStatus;
	private Set<String> filesToNotWatch;
	private ChunksRequested chunksRequested;
	private FilesRestored filesRestored;
	private ChunksStored chunksStored;
	
	private Config config;
	private Thread tcpServer;
	
	public Peer(String[] args) throws Exception {
		this.chunksRequested = new ChunksRequested();
		this.config = parseArgs(args);
		createFolders();
		createSockets();
		repStatus = ReplicationStatusFactory.getNew(config.getPeerDir());
		filesToNotWatch = new ConcurrentHashMap().newKeySet();
		filesRestored = new FilesRestored();
		chunksStored = new ChunksStored();

		mcListener = new MCListener(config, repStatus, filesToNotWatch, chunksStored);
		mdbListener = new MDBListener(config, repStatus, filesToNotWatch, chunksStored);
		mdrListener = new MDRListener(config, chunksRequested, filesRestored);

		Thread mdbListenerThr = new Thread(mcListener);
		Thread mcListenerThr = new Thread(mdbListener);
		Thread mdrListenerThr = new Thread(mdrListener);
		
		mdbListenerThr.start();
		mcListenerThr.start();
		mdrListenerThr.start();

		TCP_PORT = 4444;
		
		deleteWatcher = new DeleteWatcher(mcSocket, repStatus);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(deleteWatcher, 0, 30, TimeUnit.SECONDS);
	}

	private void initiateTCPServer() {
		if (tcpServer == null){
			tcpServer = new Thread(new TCPServer(TCP_PORT, config, chunksRequested, filesRestored));
			tcpServer.start();
		}
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
		new File(Config.getPeerDir() + "data/").mkdirs();
		new File(Config.getPeerDir() + "restored/").mkdirs();
		new File(Config.getPeerDir() + "stored/").mkdirs();
	}
	
	private Config parseArgs(String[] args) throws Exception {
		if (args.length < 8) {
			System.out.println("Usage:");
			System.out.println("java Peer Peerid Version MC_ip MC_port MDB_ip MDB_port MDR_ip MDR_port");
			System.exit(1);
		}
		Config.setPeer(args[0]);
		Config.setCurrentVersion(args[1]);
		try {
			Config.setMcIP(InetAddress.getByName(args[2]));
			Config.setMcPort(Integer.parseInt(args[3]));
			Config.setMdbIP(InetAddress.getByName(args[4]));
			Config.setMdbPort(Integer.parseInt(args[5]));
			Config.setMdrIP(InetAddress.getByName(args[6]));
			Config.setMdrPort(Integer.parseInt(args[7]));
		} catch (UnknownHostException e) {
			System.out.println("Invalid IP argument.");
			e.printStackTrace();
			System.exit(1);
		}
		
		Config.checkIPRanges();
		
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
	         Registry registry = LocateRegistry.getRegistry();
	         
	         registry.rebind(String.format("Peer_%s", peer.config.getPeerId()), stub);
	         System.out.println("Server ready"); 
	      } catch (RemoteException e) { 
	         System.err.println("Server exception: Run rmiregistry on bin folder");
	         System.exit(-1);
	      } catch (Exception e) {
	    	  System.err.println("Peer: main failed.");
	    	  e.printStackTrace();
	    	  System.exit(-2);
	      }
	}

	@Override
	public void backup(String pathname, byte desiredRepDegree) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		if (file == null) {
			System.err.println("Peer: File not found.");
			return;
		}
        pool.execute(new StoreFile(config, mdbSocket, Config.ORIG_VERSION, file, desiredRepDegree, repStatus));
	}
	
	@Override
	public void backupEnh(String pathname, byte desiredRepDegree) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		if (file == null) {
			System.err.println("Peer: File not found.");
			return;
		}
        pool.execute(new StoreFile(config, mdbSocket, Config.ENH_VERSION, file, desiredRepDegree, repStatus));
	}

	@Override
	public void restore(String pathname) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		if (file == null) {
			System.err.println("Peer: File not found.");
			return;
		}
		pool.execute(new SendRestoreFile(config, mcSocket, file, chunksRequested));
	}

	@Override
	public void restoreEnh(String pathname) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		initiateTCPServer();
		pool.execute(new SendRestoreFileEnh(config, mcSocket, file, chunksRequested, TCP_PORT));
	}

	@Override
	public void delete(String pathname) throws RemoteException {
		File file = FileProcessor.loadFile(pathname);
		pool.execute(new SendDeleteFile(Config.ORIG_VERSION, mcSocket, file, repStatus));
	}

	public void deleteEnh(String pathname) throws RemoteException{
		File file = FileProcessor.loadFile(pathname);
		pool.execute(new SendDeleteFile(Config.ENH_VERSION, mcSocket, file, repStatus));
	}

	@Override
	public PeerState state() throws RemoteException {
		return new PeerState(repStatus);
	}

	@Override
	public void reclaim(long maxDiskSpace) throws RemoteException {
		repStatus.setBytesReserved(maxDiskSpace * 1000);
		pool.execute(new ReclaimDiskSpace(config, repStatus, chunksStored));
	}



}
