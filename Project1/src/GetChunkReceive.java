import utils.ThreadUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class GetChunkReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket getChunkPacket;
	private MulticastSocket mdrSocket;
	private ChunksStoredFutures chunksStoredFutures;

	private String fileId;
	private Integer chunkNo;

	public GetChunkReceive(MulticastSocket mdrSocket, DatagramPacket packet, ChunksStoredFutures chunksStoredFutures) {
		this.getChunkPacket = packet;
		this.mdrSocket = mdrSocket;
		this.chunksStoredFutures = chunksStoredFutures;
	}

	@Override
	public void run() {
		String msg = new String(Arrays.copyOfRange(getChunkPacket.getData(), 0, getChunkPacket.getLength()), StandardCharsets.ISO_8859_1);
		String crlf = new String(CRLF);
		String[] splitMessage = msg.split(crlf);
		String head[] = splitMessage[0].split("\\s+");
		String protocolVersion = head[1];
		String senderId = head[2];
		if (senderId.equals(Config.getPeerId())) return; //no need to send chunks if I am asking to restore
		fileId = head[3];
		chunkNo = Integer.parseInt(head[4]);
		String chunkFilePath = Config.getPeerDir() + "stored/" + FileProcessor.createChunkName(fileId,chunkNo);

		boolean success = chunksStoredFutures.getFuture(fileId,chunkNo);
		if (!success) {
            System.err.println("GetChunkReceive: Could not get future for: " + FileProcessor.createChunkName(fileId,chunkNo));
            return;
        }
		Path path = Paths.get(chunkFilePath);
		byte[] data;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			System.out.format("Error reading %s.\n", chunkFilePath);
			return;
		}
		ThreadUtils.waitBetween(0,400);

		if (protocolVersion.equals(Config.ORIG_VERSION)){
			SendChunkUDP(data);
		}else if (protocolVersion.equals(Config.ENH_VERSION) && Config.isEnhanced()) {
			String[] head2 = splitMessage[1].split("\\s+");
			String hostname = head2[0];
			Integer port = Integer.parseInt(head2[1]);
			SendChunkTCP(data, hostname, port);
		}else{
			System.err.println("GetChunkReceive: Protocol Version unknown: " + protocolVersion);
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
		    System.out.println("GetChunk Received: sent chunk with bytes: " + data.length);
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

	private byte[] makeByteArrayPacket(byte[] body){
        byte[] header = Messages.getCHUNKHeader(fileId,chunkNo);
        byte[] fullPacket = new byte[header.length + body.length];
        System.arraycopy(header,0,fullPacket,0,header.length);
        System.arraycopy(body,0,fullPacket,header.length,body.length);
		return fullPacket;
	}

	private DatagramPacket makeDatagramPacket(byte[] body) {
		byte[] fullPacket = makeByteArrayPacket(body);
		return new DatagramPacket(fullPacket, fullPacket.length, Config.getMdrIP(), Config.getMdrPort());
	}



}
