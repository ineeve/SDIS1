import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Peer {
	
	private Initiator initiator;
	private MCListener mcListener;
	private MDBListener mdbListener;
	private MDRListener mdrListener;
	
	private Config config;
	
	public Peer(String[] args) {
		this.config = parseArgs(args);
		createFolders();
		ReplicationStatus repStatus = ReplicationStatusFactory.getNew(config.getPeerDir());
		initiator = new Initiator(config, repStatus);
		mcListener = new MCListener(config, repStatus);
		mdbListener = new MDBListener(config, repStatus);
		mdrListener = new MDRListener(config);
		
		Thread initiatorThr = new Thread(initiator);
		Thread mdbListenerThr = new Thread(mcListener);
		Thread mcListenerThr = new Thread(mdbListener);
		Thread mdrListenerThr = new Thread(mdrListener);
		
		mdbListenerThr.start();
		mcListenerThr.start();
		mdrListenerThr.start();
		initiatorThr.start();
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

	public static void main(String[] args) {
		//Non initiator peers: java Peer id MC_ip MC_port MDB_ip MDB_port
		//Initiator peers: java Peer id MC_ip MC_port MDB_ip MDB_port filename replication
		new Peer(args);
	}

}
