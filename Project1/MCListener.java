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
	private BackupStatus backupStatus;
	private MulticastSocket mcSocket;

	public MCListener(String peerId, InetAddress mcIP, int mcPort, BackupStatus backupStatus) {
		this.peerId = peerId;
		this.mcIP = mcIP;
		this.mcPort = mcPort;
		this.backupStatus = backupStatus;
		try {
			mcSocket = new MulticastSocket(mcPort);
			mcSocket.joinGroup(mcIP);
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
			pool.execute(new StoredReceive(chunkPacket, backupStatus));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
