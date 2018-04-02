import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

import utils.ThreadUtils;

public class SendDeleteFile implements Runnable {

	private MulticastSocket mcSocket;
	private File file;
	private String fileId;
	private String version;

	public SendDeleteFile(String version, MulticastSocket mcSocket, File file, ReplicationStatus replicationStatus) {
		FileProcessor fileProcessor = new FileProcessor();
		this.version = version;
		this.mcSocket = mcSocket;
		this.file = file;
		this.fileId = fileProcessor.getFileId(file);
		replicationStatus.setDeriredRepDegreeOfFile(fileId, (byte) 0);
	}

	@Override
	public void run() {
		DatagramPacket packet = makeDeletePacket();
		sendPacket(packet);
	}

	/**
	 * Tries to send packet over MC 10 times.
	 * @param packet
	 */
	private void sendPacket(DatagramPacket packet) {
		boolean packetSent = false;
		int numTries = 0;
		while (!packetSent && numTries < 10) {
			packetSent = false;
			try {
				mcSocket.send(packet);
				packetSent = true;
			} catch (IOException e) {
				//buffer is full
	            ThreadUtils.waitBetween(10,400);
			}
			numTries++;
		}
		System.out.println("SendDeleteFile: Sent delete packet");
	}

	private DatagramPacket makeDeletePacket() {
		byte[] deleteMsg = Messages.getDELETEHeader(fileId, version);
		DatagramPacket packet = new DatagramPacket(deleteMsg, deleteMsg.length, Config.getMcIP(), Config.getMcPort());
		return packet;
	}
}
