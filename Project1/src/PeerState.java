import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import utils.ThreadUtils;

public class PeerState implements Serializable {

	private ArrayList<BackedUpFile> backedUpFiles;
//	private ArrayList<StoredChunk> storedChunks;
	private long maxDiskSpace; //KBytes
	private long usedDiskSpace; //KBytes
	
	public PeerState(ReplicationStatus repStatus) {
		backedUpFiles = repStatus.getFiles();
		maxDiskSpace = repStatus.getBytesReserved() / 1000;
		usedDiskSpace = repStatus.getBytesUsed() / 1000;
	}

	public void present() {
		System.out.println("BACKED UP FILES");
		for (final BackedUpFile file : backedUpFiles) {
			file.present();
		}
		System.out.println();
		
		System.out.println("STORED CHUNKS");
//		for (final StoredChunk chunk : storedChunks) {
//			chunk.present();
//		}
		System.out.println();
		
		System.out.format("Maximum disk space: %d KB\n", maxDiskSpace);
		System.out.format("Used disk space: %d KB\n", usedDiskSpace);
	}

//	public void backupFile(String pathname, String fileId, byte desiredRepDegree) {
//		backedUpFiles.add(new BackedUpFile(pathname, fileId, desiredRepDegree));
//	}
//
//	public void setNumChunks(String fileId, int chunkNo) {
//		for (BackedUpFile file : backedUpFiles) {
//			if (file.getFileId() == fileId) {
//				file.initChunks(chunkNo);
//			}
//		}
//	}
}
