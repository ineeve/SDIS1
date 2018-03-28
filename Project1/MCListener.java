import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MCListener implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();
	
	private Config config;
	private ReplicationStatus replicationStatus;
	private MulticastSocket mcSocket;
	private MulticastSocket mdrSocket;

	public MCListener(Config config, ReplicationStatus replicationStatus) {
		this.config = config;
		this.replicationStatus = replicationStatus;
		try {
			mcSocket = new MulticastSocket(config.getMcPort());
			mcSocket.joinGroup(config.getMcIP());
			mdrSocket = new MulticastSocket(config.getMdrPort());
			mdrSocket.joinGroup(config.getMdrIP());
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
			pool.execute(new GetChunkReceive(config, mdrSocket, chunkPacket));
		}
	}

}
