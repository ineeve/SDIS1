import java.util.ArrayList;

public class PeerState {

	private ArrayList<BackedUpFile> backedUpFiles;
//	private ArrayList<StoredChunk> storedChunks;
	private int maxDiskSpace; //KBytes
	private int usedDiskSpace; //KBytes
	
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
	
}
