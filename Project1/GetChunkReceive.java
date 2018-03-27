import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetChunkReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket getChunkPacket;
	private MulticastSocket mdrSocket;

	private String peerId;
	private InetAddress mdrIP;
	private int mdrPort;

	public GetChunkReceive(MulticastSocket mdrSocket, InetAddress mdrIP, int mdrPort, String peerId, DatagramPacket packet) {
		this.peerId = peerId;
		this.getChunkPacket = packet;
		this.mdrSocket = mdrSocket;
		this.mdrIP = mdrIP;
		this.mdrPort = mdrPort;
	}

	@Override
	public void run() {
		String msg = new String(getChunkPacket.getData(), Charset.forName("ISO_8859_1")).trim();
		String crlf = new String(CRLF);
		String[] splittedMessage = msg.trim().split(crlf + crlf);
		String head[] = splittedMessage[0].split("\\s+");
		String senderId = head[2];
		String fileId = head[3];
		Integer chunkNo = Integer.parseInt(head[4]);
		
		String chunkFilename = String.format("data/%s_%d.out", fileId, chunkNo);
		Path path = Paths.get(chunkFilename);
		byte[] data;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			System.out.format("Error reading %s.\n", chunkFilename);
			return;
		}
		waitMs(0, 400);
		sendChunk(fileId, chunkNo, data);
	}

	private void sendChunk(String fileId, Integer chunkNo, byte[] data) {
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, data);
		try {
			mdrSocket.send(chunkPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private DatagramPacket makeChunkPacket(String fileId, Integer chunkNo, byte[] data) {
		String msgStr = String.format("CHUNK 1.0 %s %s %d %s%s", peerId, fileId, chunkNo, CRLF, CRLF);
		msgStr += new String(data, Charset.forName("ISO_8859_1"));
		byte[] msg = msgStr.getBytes(Charset.forName("ISO_8859_1"));
		return new DatagramPacket(msg, msg.length, mdrIP, mdrPort);
	}

	private void waitMs(int low, int high) {
		int value = (int) (Math.random() * (high - low) + low);
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			System.out.println("Thread Interrupted");
			Thread.currentThread().interrupt();
		}
	}

}
