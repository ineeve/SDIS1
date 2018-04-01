import utils.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MCListener implements Runnable {



    private ExecutorService pool = Executors.newCachedThreadPool();
	
	private Config config;
	private ReplicationStatus replicationStatus;
    private ChunksStored chunksStored;

	private MulticastSocket mcSocket;
	private MulticastSocket mdrSocket;
	private MulticastSocket mdbSocket;

	private Set<String> filesToNotWatch; //this is thread-safe

	public MCListener(Config config, ReplicationStatus replicationStatus, Set<String> filesToNotWatch, ChunksStored chunksStored) {
		this.filesToNotWatch = filesToNotWatch;
	    this.config = config;
		this.replicationStatus = replicationStatus;
		this.chunksStored = chunksStored;
		createWatcher();
		try {
			mcSocket = new MulticastSocket(config.getMcPort());
			mcSocket.joinGroup(config.getMcIP());
			mdrSocket = new MulticastSocket(config.getMdrPort());
			mdrSocket.joinGroup(config.getMdrIP());
			mdbSocket = new MulticastSocket(config.getMdbPort());
			mdbSocket.joinGroup(config.getMdbIP());
		} catch (IOException e) {
			System.out.println("Failed to start MCListener service.");
			e.printStackTrace();
		}
	}

	private void createWatcher(){
		pool.execute(new WatchStored(filesToNotWatch,config,mcSocket, chunksStored));
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
			pool.execute(new GetChunkReceive(config, mdrSocket, packet, chunksStored));
		} else if (Messages.isRemoved(packet)){
			pool.execute(new HandleRemoved(config, replicationStatus, packet, mdbSocket));
		} else if (Messages.isDelete(packet)) {
			pool.execute(new DeleteReceive(config, packet, filesToNotWatch));
		} else {
			System.out.println("Caught unhandled message in MCListener");
		}
	}

}
