import java.util.ArrayList;

public class BackedUpFile {

	private String pathname;
	private String fileId;
	private int desiredRepDegree;
//	ArrayList<Chunk> chunks;

	public void present() {
		System.out.format("- %s | %s | %d\n", pathname, fileId, desiredRepDegree);
//		for (final Chunk chunk : chunks) {
//			chunk.present();
//		}
	}
	
}
