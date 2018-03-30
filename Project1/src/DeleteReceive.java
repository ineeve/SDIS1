import utils.Pair;

import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeleteReceive implements Runnable {

	private Config config;
	private DatagramPacket packet;
	private Set<String> filesToNotWatch;

	public DeleteReceive(Config config, DatagramPacket packet, Set<String> filesNotWatch) {
		this.config = config;
		this.packet = packet;
		this.filesToNotWatch = filesNotWatch;
	}

	@Override
	public void run() {
		String fileId = getFileId(packet);
		filesToNotWatch.add(fileId);
		deleteChunks(fileId);
	}

	private void deleteChunks(String fileId) {
		String folder = String.format("Peer_%s/stored", config.getPeerId());
		FileProcessor.deleteFilesStartingWith(fileId, folder);
		System.out.println("Deleted all chunks of file: " + fileId);
	}

	private String getFileId(DatagramPacket packet) {
		String msgStr = new String(packet.getData(), Charset.forName("ISO_8859_1")).trim();
		String fileId = msgStr.split(" ")[3];
		return fileId;
	}

}
