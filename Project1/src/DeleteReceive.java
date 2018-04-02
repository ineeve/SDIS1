import utils.ThreadUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.util.Set;

public class DeleteReceive implements Runnable {

	private DatagramPacket packet;
	private Set<String> filesToNotWatch;
	private MulticastSocket mcSocket;

	public DeleteReceive(DatagramPacket packet, Set<String> filesNotWatch, MulticastSocket mcSocket) {
		this.packet = packet;
		this.filesToNotWatch = filesNotWatch;
		this.mcSocket = mcSocket;
	}

	@Override
	public void run() {
		String fileId = getFileId(packet);
		filesToNotWatch.add(fileId);
		int numChunksDeleted;
		if ((numChunksDeleted = deleteChunks(fileId)) > 0) {
		    System.out.format("%d of %d chunks were deleted", numChunksDeleted, fileId);
			if (Config.isEnhanced() && getVersion(packet).equals(Config.ENH_VERSION)) {
				DatagramPacket confirmDeletePacket = makeConfirmationPacket(fileId);
				sendConfirmPacket(confirmDeletePacket, fileId);
			}
		}
	}

	private void sendConfirmPacket(DatagramPacket confirmDeletePacket, String fileId) {
		boolean wasSent = false;
		ThreadUtils.waitBetween(0,400);
		do{
			try {
				System.out.println("Sending confirmation of deletion of file: " + fileId);
				mcSocket.send(confirmDeletePacket);
				wasSent = true;
			} catch (IOException e) {
				ThreadUtils.waitBetween(0,400);
			}
		}while(!wasSent);
	}

	/**
	 * Delete all stored chunks of a given file
	 * @param fileId
	 * @return number of files deleted
	 */
	private int deleteChunks(String fileId) {
		String folder = String.format("Peer_%s/stored", Config.getPeerId());
		return FileProcessor.deleteFilesStartingWith(fileId, folder);
	}

	private String getFileId(DatagramPacket packet) {
		String msgStr = new String(packet.getData(), Charset.forName("ISO_8859_1")).trim();
		String fileId = msgStr.split(" ")[3];
		return fileId;
	}
	
	private String getVersion(DatagramPacket packet) {
		String msgStr = new String(packet.getData(), Charset.forName("ISO_8859_1")).trim();
		String version = msgStr.split(" ")[1];
		return version;
	}

	private DatagramPacket makeConfirmationPacket(String fileId){
		byte[] msg = Messages.getDELETEDHeader(fileId);
		return new DatagramPacket(msg, msg.length, Config.getMcIP(), Config.getMcPort());
	}

}
