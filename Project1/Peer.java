import java.net.InetAddress;
import java.net.UnknownHostException;

public class Peer {
	
	private Initiator initiator;
	private MCListener mcListener;
	private MDBListener mdbListener;
	
	private String peerId;
	private InetAddress mcIP;
	private int mcPort;
	private InetAddress mdbIP;
	private int mdbPort;
	
	public Peer(String[] args) {
		parseArgs(args);
		BackupStatus backupStatus = new BackupStatus();
		initiator = new Initiator(peerId, mcIP, mcPort, mdbIP, mdbPort, backupStatus);
//		mcListener = new MCListener(peerId, mcIP, mcPort, mdbIP, mdbPort, backupStatus);
		mdbListener = new MDBListener(peerId, mcIP, mcPort, mdbIP, mdbPort);
		
		Thread initiatorThr = new Thread(initiator);
		Thread mdbListenerThr = new Thread(mcListener);
		Thread mcListenerThr = new Thread(mdbListener);
		
		mdbListenerThr.start();
		mcListenerThr.start();
		initiatorThr.start();
	}
	
	private void parseArgs(String[] args) {
		if (args.length < 5) {
			System.out.println("Usage:");
			System.out.println("java Peerid MC_ip MC_port MDB_ip MDB_port");
			System.exit(1);
		}
		peerId = args[0];
		try {
			mcIP = InetAddress.getByName(args[1]);
			mcPort = Integer.parseInt(args[2]);
			mdbIP = InetAddress.getByName(args[3]);
			mdbPort = Integer.parseInt(args[4]);
		} catch (UnknownHostException e) {
			System.out.println("Invalid IP argument.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		//Non initiator peers: java Peer id MC_ip MC_port MDB_ip MDB_port
		//Initiator peers: java Peer id MC_ip MC_port MDB_ip MDB_port filename replication
		
		Peer peer = new Peer(args);
	}

}
