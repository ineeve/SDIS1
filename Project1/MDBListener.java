import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MDBListener implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();
	
	private static final byte[] CRLF = {0xD, 0xA};
	private ConcurrentHashMap<String,ArrayList<Integer>> chunksStored = new ConcurrentHashMap<String, ArrayList<Integer>>(); //filename to chunks
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private String peerId;
	private InetAddress mdbIP;
	private int mdbPort;
	private InetAddress mcIP;
	private int mcPort;

	public MDBListener(String peerId, InetAddress mcIP, int mcPort, InetAddress mdbIP, int mdbPort) {
		this.peerId = peerId;
		this.mdbIP = mdbIP;
		this.mdbPort = mdbPort;
		this.mcIP = mcIP;
		this.mcPort = mcPort;
		try {
			mcSocket = new MulticastSocket(mcPort);
			mcSocket.joinGroup(mcIP);
			mdbSocket = new MulticastSocket(mdbPort);
			mdbSocket.joinGroup(mdbIP);
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
			pool.execute(new PutChunkReceive(putChunkPacket, peerId, chunksStored, mcSocket, mcIP, mcPort));
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
