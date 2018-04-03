import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.ThreadUtils;

public class MDBListener implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();

	private ReplicationStatus repStatus;
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private Set<String> filesToNotWatch;

	public MDBListener(ReplicationStatus repStatus, Set<String> filesToNotWatch) {
		this.filesToNotWatch = filesToNotWatch;
		this.repStatus = repStatus;
		try {
			mcSocket = new MulticastSocket(Config.getMcPort());
			mcSocket.joinGroup(Config.getMcIP());
			mdbSocket = new MulticastSocket(Config.getMdbPort());
			mdbSocket.joinGroup(Config.getMdbIP());
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (Messages.isPutChunk(putChunkPacket)){
			if (Messages.isEnhanced(putChunkPacket)) {
				ThreadUtils.waitBetween(10, 400);
			}
            pool.execute(new PutChunkReceive(putChunkPacket, repStatus, mcSocket, filesToNotWatch));
        }else{
            System.out.println("Caught unhandled message in MDBListener");
        }
		
	}

	@Override
	public void run() {
		while (true) {
			receiveChunk();
		}
	}

}
