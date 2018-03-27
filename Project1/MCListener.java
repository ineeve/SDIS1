import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MCListener implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();
	
	private String peerId;
	private InetAddress mcIP;
	private int mcPort;
	private InetAddress mdrIP;
	private int mdrPort;
	private ReplicationStatus replicationStatus;
	private MulticastSocket mcSocket;
	private MulticastSocket mdrSocket;

	public MCListener(String peerId, InetAddress mcIP, int mcPort, InetAddress mdrIP, int mdrPort, ReplicationStatus replicationStatus) {
		this.peerId = peerId;
		this.mcIP = mcIP;
		this.mcPort = mcPort;
		this.mdrIP = mdrIP;
		this.mdrPort = mdrPort;
		this.replicationStatus = replicationStatus;
		try {
			mcSocket = new MulticastSocket(mcPort);
			mcSocket.joinGroup(mcIP);
			mdrSocket = new MulticastSocket(mdrPort);
			mdrSocket.joinGroup(mdrIP);
		} catch (IOException e) {
			System.out.println("Failed to start MCListener service.");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			receiveChunk();
		}
	}

	private void receiveChunk() {
		int datagramMaxSize = (int) Math.pow(2,16);
		DatagramPacket chunkPacket = new DatagramPacket(new byte[datagramMaxSize], datagramMaxSize);
		try {
			mcSocket.receive(chunkPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (Messages.isStored(chunkPacket)) {
			pool.execute(new StoredReceive(chunkPacket, replicationStatus));
		} else if (Messages.isGetChunk(chunkPacket)) {
			pool.execute(new GetChunkReceive(mdrSocket, mdrIP, mdrPort, peerId, chunkPacket));
		}
	}

}
