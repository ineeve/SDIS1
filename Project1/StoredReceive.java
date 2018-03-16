import java.net.DatagramPacket;

/**
 * Adds peer ID of received STORED to entry in BackupStatus.
 */
public class StoredReceive implements Runnable {

	private static final String CRLF = "\r\n";
	
	private DatagramPacket chunkPacket;
	private BackupStatus backupStatus;

	public StoredReceive(DatagramPacket chunkPacket, BackupStatus backupStatus) {
		this.chunkPacket = chunkPacket;
		this.backupStatus = backupStatus;
	}

	@Override
	public void run() {
		String receivedMsg = new String(chunkPacket.getData());
		String crlf = new String(CRLF);
		String[] splittedMessage = receivedMsg.trim().split(crlf + crlf);
		String head[] = splittedMessage[0].split("\\s+");
		String peerId = head[2];
		String fileId = head[3];
		Integer chunkNo = Integer.parseInt(head[4]);
		backupStatus.addPeerId(peerId, fileId, chunkNo);
	}

}
