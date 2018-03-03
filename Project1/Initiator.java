import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Initiator implements Runnable {

	private MulticastSocket mdbSocket;
	private MulticastSocket mcSocket;
	private String peerId;
	
	public Initiator(String peerId, InetAddress mcIP, int mcPort, InetAddress mdbIP, int mdbPort) throws IOException {
		this.peerId = peerId;
		mcSocket = new MulticastSocket(mcPort);
		mcSocket.joinGroup(mcIP);
		mdbSocket = new MulticastSocket(mdbPort);
		mdbSocket.joinGroup(mdbIP);
	}
	
	// Should CRLF be "DA"?
	private DatagramPacket makeChunkPacket(String fileId, int chunkNo, byte repDeg, String data) {
		String putChunkMsg = "PUTCHUNK 1.0 " + peerId + " " + fileId + " " + chunkNo + " " + repDeg + " DADA";
		putChunkMsg += data;
		DatagramPacket packet = new DatagramPacket(putChunkMsg.getBytes(), putChunkMsg.length());
		return packet;
	}
	
	private void storeChunk(String fileId, int chunkNo, byte repDeg, String data) throws IOException {
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, repDeg, data);
		int listeningInterval = 1;
		for (int i = 1; i <= 5; i++) {
			mdbSocket.send(chunkPacket);
			
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
