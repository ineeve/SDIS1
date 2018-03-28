import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MDBListener implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();
	
	private ConcurrentHashMap<String,ArrayList<Integer>> chunksStored = new ConcurrentHashMap<String, ArrayList<Integer>>(); //filename to chunks
	private ReplicationStatus repStatus;
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private Config config;

	public MDBListener(Config config, ReplicationStatus repStatus) {
		this.config = config;
		this.repStatus = repStatus;
		try {
			mcSocket = new MulticastSocket(config.getMcPort());
			mcSocket.joinGroup(config.getMcIP());
			mdbSocket = new MulticastSocket(config.getMdbPort());
			mdbSocket.joinGroup(config.getMdbIP());
		} catch (IOException e) {
			System.out.println("Failed to start MDBListener service.");
			e.printStackTrace();
		}
	}

	public void receiveChunk(){
		int datagramMaxSize = (int) Math.pow(2,16);
		DatagramPacket putChunkPacket = new DatagramPacket(new byte[datagramMaxSize], datagramMaxSize);
		try {
			mdbSocket.receive(putChunkPacket);
			pool.execute(new PutChunkReceive(config, putChunkPacket, chunksStored, repStatus, mcSocket));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			receiveChunk();
		}
	}

}
