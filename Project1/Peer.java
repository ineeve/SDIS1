import java.net.InetAddress;

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
		initiator = new Initiator(peerId, mcIP, mcPort, mdbIP, mdbPort);
		mcListener = new MCListener(peerId, mcIP, mcPort, mdbIP, mdbPort);
		mdbListener = new MDBListener(peerId, mcIP, mcPort, mdbIP, mdbPort);
		
		Thread initiatorThr = new Thread(initiator);
		Thread mdbListenerThr = new Thread(mcListener);
		Thread mcListenerThr = new Thread(mdbListener);
		
		mdbListenerThr.start();
		mcListenerThr.start();
		initiatorThr.start();
	}
	
	private void parseArgs(String[] args) {
		peerId = args[0];
	}

	public static void main(String[] args) {
		//Non initiator peers: java Peer id MC_ip MC_port MDB_ip MDB_port
		//Initiator peers: java Peer id MC_ip MC_port MDB_ip MDB_port filename replication
		
		Peer peer = new Peer(args);		
	}

}
