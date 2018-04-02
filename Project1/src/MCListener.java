import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MCListener implements Runnable {



    private ExecutorService pool = Executors.newCachedThreadPool();
	
	private ReplicationStatus replicationStatus;
    private ChunksStored chunksStored;

	private MulticastSocket mcSocket;
	private MulticastSocket mdrSocket;
	private MulticastSocket mdbSocket;

	private Set<String> filesToNotWatch; //this is thread-safe

	public MCListener(ReplicationStatus replicationStatus, Set<String> filesToNotWatch, ChunksStored chunksStored) {
		this.filesToNotWatch = filesToNotWatch;
		this.replicationStatus = replicationStatus;
		this.chunksStored = chunksStored;
		createWatcher();
		try {
			mcSocket = new MulticastSocket(Config.getMcPort());
			mcSocket.joinGroup(Config.getMcIP());
			mdrSocket = new MulticastSocket(Config.getMdrPort());
			mdrSocket.joinGroup(Config.getMdrIP());
			mdbSocket = new MulticastSocket(Config.getMdbPort());
			mdbSocket.joinGroup(Config.getMdbIP());
		} catch (IOException e) {
			System.out.println("Failed to start MCListener service.");
			e.printStackTrace();
		}
	}

	private void createWatcher(){
		pool.execute(new WatchStored(filesToNotWatch, mcSocket, chunksStored));
	}

	@Override
	public void run() {
		while (true) {
			receiveChunk();
		}
	}

	private void receiveChunk() {
		int datagramMaxSize = (int) Math.pow(2,16);
		DatagramPacket packet = new DatagramPacket(new byte[datagramMaxSize], datagramMaxSize);
		try {
			mcSocket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (Messages.isStored(packet)) {
			pool.execute(new StoredReceive(packet, replicationStatus));
		} else if (Messages.isGetChunk(packet)) {
			pool.execute(new GetChunkReceive(mdrSocket, packet, chunksStored));
		} else if (Messages.isRemoved(packet)){
			pool.execute(new RemovedReceive(replicationStatus, packet, mdbSocket));
		} else if (Messages.isDelete(packet)) {
			pool.execute(new DeleteReceive(packet, filesToNotWatch, mcSocket));
		} else if (Messages.isDeleted(packet)){
			pool.execute(new DeletedReceive(packet, replicationStatus));
		} else {
			System.out.println("Caught unhandled message in MCListener");
		}
	}

}
