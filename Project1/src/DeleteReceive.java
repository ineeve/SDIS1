import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class DeleteReceive implements Runnable {

	private Config config;
	private DatagramPacket packet;

	public DeleteReceive(Config config, DatagramPacket packet) {
		this.config = config;
		this.packet = packet;
	}

	@Override
	public void run() {
		String fileId = getFileId(packet);
		deleteChunks(fileId);
	}

	private void deleteChunks(String fileId) {
		String folder = String.format("Peer_%s/stored", config.getPeerId());
		FileProcessor.deleteFilesStartingWith(fileId, folder);
	}

	private String getFileId(DatagramPacket packet) {
		String msgStr = new String(packet.getData(), Charset.forName("ISO_8859_1")).trim();
		String fileId = msgStr.split(" ")[3];
		return fileId;
	}

}
