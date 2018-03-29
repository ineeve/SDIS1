import utils.ThreadUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetChunkReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket getChunkPacket;
	private MulticastSocket mdrSocket;

	private Config config;

	public GetChunkReceive(Config config, MulticastSocket mdrSocket, DatagramPacket packet) {
		this.config = config;
		this.getChunkPacket = packet;
		this.mdrSocket = mdrSocket;
	}

	@Override
	public void run() {
		String msg = new String(getChunkPacket.getData(), Charset.forName("ISO_8859_1")).trim();
		String crlf = new String(CRLF);
		String[] splitMessage = msg.trim().split(crlf + crlf);
		String head[] = splitMessage[0].split("\\s+");
		String senderId = head[2];
		if (senderId.equals(config.getPeerId())) return; //no need to send chunks if I am asking to restore
		String fileId = head[3];
		Integer chunkNo = Integer.parseInt(head[4]);
		
		String chunkFilename = String.format("%sstored/%s/%d.out", config.getPeerDir(), fileId, chunkNo);
		Path path = Paths.get(chunkFilename);
		byte[] data;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			System.out.format("Error reading %s.\n", chunkFilename);
			return;
		}
        ThreadUtils.waitBetween(10,400);
		sendChunk(fileId, chunkNo, data);
	}

	private void sendChunk(String fileId, Integer chunkNo, byte[] data) {
		boolean wasSent = false;
		DatagramPacket chunkPacket = makeChunkPacket(fileId, chunkNo, data);
		do{
			try {
				mdrSocket.send(chunkPacket);
				wasSent = true;
			} catch (IOException e) {
				System.out.println("Full buffer");
                ThreadUtils.waitBetween(10,100);
			}
		}while(!wasSent);

	}

	private DatagramPacket makeChunkPacket(String fileId, Integer chunkNo, byte[] data) {
		String msgStr = String.format("CHUNK %s %s %s %d %s%s",config.getProtocolVersion(), config.getPeerId(), fileId, chunkNo, CRLF, CRLF);
		msgStr += new String(data, Charset.forName("ISO_8859_1"));
		byte[] msg = msgStr.getBytes(Charset.forName("ISO_8859_1"));
		return new DatagramPacket(msg, msg.length, config.getMdrIP(), config.getMdrPort());
	}



}
