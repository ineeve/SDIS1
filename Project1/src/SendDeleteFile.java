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
	private ReplicationStatus repStatus;

	public SendDeleteFile(String version, MulticastSocket mcSocket, File file, ReplicationStatus replicationStatus) {
		this.version = version;
		this.mcSocket = mcSocket;
		this.file = file;
		this.fileId = FileProcessor.getFileId(file);
		this.repStatus = replicationStatus;
		replicationStatus.setDeriredRepDegreeOfFile(fileId, (byte) 0);
	}
	
	public SendDeleteFile(String version, MulticastSocket mcSocket, String fileId, ReplicationStatus replicationStatus) {
		this.version = version;
		this.mcSocket = mcSocket;
		this.fileId = fileId;
		this.repStatus = replicationStatus;
		replicationStatus.setDeriredRepDegreeOfFile(fileId, (byte) 0);
	}

	@Override
	public void run() {
		DatagramPacket packet = makeDeletePacket();
		sendPacket(packet);
		if (Config.isEnhanced() && version.equals(Config.ENH_VERSION)) {
			repStatus.addDeleteWatch(fileId);
		}
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
