import java.net.DatagramPacket;
import java.nio.charset.Charset;

/**
 * Adds peer ID of received STORED to entry in BackupStatus.
 */
public class StoredReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket chunkPacket;
	private ReplicationStatus backupStatus;

	public StoredReceive(DatagramPacket chunkPacket, ReplicationStatus backupStatus) {
		this.chunkPacket = chunkPacket;
		this.backupStatus = backupStatus;
	}

	@Override
	public void run() {
		String receivedMsg = new String(chunkPacket.getData(), Charset.forName("ISO_8859_1"));
		String crlf = new String(CRLF);
		String[] splittedMessage = receivedMsg.trim().split(crlf + crlf);
		String head[] = splittedMessage[0].split("\\s+");
		String peerId = head[2];
		String fileId = head[3];
		Integer chunkNo = Integer.parseInt(head[4]);
		backupStatus.stored_addPeerId(peerId, fileId, chunkNo);
	}

}