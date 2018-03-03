import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Initiator implements Runnable {

	private static final byte[] CRLF = {0xD, 0xA};
	
	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private String peerId;
	
	ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public Initiator(String peerId, InetAddress mcIP, int mcPort, InetAddress mdbIP, int mdbPort) throws IOException {
		this.peerId = peerId;
		mcSocket = new MulticastSocket(mcPort);
		mcSocket.joinGroup(mcIP);
		mdbSocket = new MulticastSocket(mdbPort);
		mdbSocket.joinGroup(mdbIP);
	}
	
	// Should CRLF be "DA"?
	private DatagramPacket makeChunkPacket(String fileId, int chunkNo, byte repDeg, String data) {
		String putChunkMsg = "PUTCHUNK 1.0 " + peerId + " " + fileId + " " + chunkNo + " " + repDeg + CRLF + CRLF;
		putChunkMsg += data;
		DatagramPacket packet = new DatagramPacket(putChunkMsg.getBytes(), putChunkMsg.length());
		return packet;
	}
	
	private void storeChunk(String fileId, int chunkNo, byte repDeg, String data) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		Future<Integer> future;
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, repDeg, data);
		int listeningInterval = 1; //seconds
		for (int i = 1; i <= 5; i++) {
			mdbSocket.send(chunkPacket);
			future = executor.submit(new ConfirmationListener(mcSocket));
			int numConfirmations = future.get(listeningInterval, TimeUnit.SECONDS);
			if (numConfirmations >= repDeg) {
				// Missing storage of numConfirmations.
				break;
			}
			listeningInterval *= 2;
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}

class ConfirmationListener implements Callable<Integer> {
	private MulticastSocket mcSocket;
	
    public ConfirmationListener(MulticastSocket mcSocket) {
		this.mcSocket = mcSocket;
	}

	@Override
    public Integer call() throws Exception {
        int numConfirmations = 0;
        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
        
        
        mcSocket.receive(packet);
        
        return numConfirmations;
    }
}