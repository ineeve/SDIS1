import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

import utils.ThreadUtils;

public class SendDeleteFile implements Runnable {
	
	private Config config;
	private MulticastSocket mcSocket;
	private File file;
	private String fileId;

	public SendDeleteFile(Config config, MulticastSocket mcSocket, File file) {
		FileProcessor fileProcessor = new FileProcessor();
		this.config = config;
		this.mcSocket = mcSocket;
		this.file = file;
		this.fileId = fileProcessor.getFileId(file);
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
		byte[] deleteMsg = Messages.getDELETEHeader(fileId);
		DatagramPacket packet = new DatagramPacket(deleteMsg, deleteMsg.length, config.getMcIP(), config.getMcPort());
		return packet;
	}
}
