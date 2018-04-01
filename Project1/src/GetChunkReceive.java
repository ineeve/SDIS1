import utils.ThreadUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetChunkReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket getChunkPacket;
	private MulticastSocket mdrSocket;

	private Config config;

	private String fileId;
	private Integer chunkNo;

	public GetChunkReceive(Config config, MulticastSocket mdrSocket, DatagramPacket packet) {
		this.config = config;
		this.getChunkPacket = packet;
		this.mdrSocket = mdrSocket;
	}

	@Override
	public void run() {
		String msg = new String(getChunkPacket.getData(), Charset.forName("ISO_8859_1")).trim();
		String crlf = new String(CRLF);
		String[] splitMessage = msg.trim().split(crlf);
		String head[] = splitMessage[0].split("\\s+");
		String protocolVersion = head[1];
		String senderId = head[2];
		if (senderId.equals(config.getPeerId())) return; //no need to send chunks if I am asking to restore
		fileId = head[3];
		chunkNo = Integer.parseInt(head[4]);
		String chunkFilename = config.getPeerDir() + "stored/" + FileProcessor.createChunkName(fileId,chunkNo);
		Path path = Paths.get(chunkFilename);
		byte[] data;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			System.out.format("Error reading %s.\n", chunkFilename);
			return;
		}
		ThreadUtils.waitBetween(0,400);

		if (protocolVersion.equals("1.0")){
			SendChunkUDP(data);
		}else if (protocolVersion.equals("2.0")){
			String[] head2 = splitMessage[1].split("\\s+");
			String hostname = head2[0];
			Integer port = Integer.parseInt(head2[1]);
			SendChunkTCP(data, hostname, port);
		}else{
			System.err.println("Protocol Version unknown: " + protocolVersion);
		}
	}

	private void SendChunkTCP(byte[] data, String hostname, Integer port) {
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(hostname, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
		    System.out.println("GetChunk Received: using TCP");
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			byte[] tcpPacket = makeByteArrayPacket(data);
			outToServer.write(tcpPacket);
			outToServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void SendChunkUDP(byte[] data) {
		boolean wasSent = false;
		DatagramPacket chunkPacket = makeDatagramPacket(data);
		do{
			try {
				mdrSocket.send(chunkPacket);
				wasSent = true;
                System.out.println("GetChunkReceive: Sent Chunk " + chunkNo);
			} catch (IOException e) {
				System.out.println("GetChunkReceive: Full buffer on chunk " + chunkNo);
                ThreadUtils.waitBetween(0,400);
			}
		}while(!wasSent);

	}

	private byte[] makeByteArrayPacket(byte[] data){
		String msgStr = String.format("CHUNK 2.0 %s %s %d %s%s", config.getPeerId(), fileId, chunkNo, CRLF, CRLF);
		msgStr += new String(data, Charset.forName("ISO_8859_1"));
		return msgStr.getBytes(Charset.forName("ISO_8859_1"));
	}

	private DatagramPacket makeDatagramPacket(byte[] data) {
		String msgStr = String.format("CHUNK 1.0 %s %s %d %s%s", config.getPeerId(), fileId, chunkNo, CRLF, CRLF);
		msgStr += new String(data, Charset.forName("ISO_8859_1"));
		byte[] msg = msgStr.getBytes(Charset.forName("ISO_8859_1"));
		return new DatagramPacket(msg, msg.length, config.getMdrIP(), config.getMdrPort());
	}



}
