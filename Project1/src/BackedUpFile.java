import java.io.Serializable;
import java.util.ArrayList;

public class BackedUpFile implements Serializable {

	private String pathname;
	private String fileId;
	private int desiredRepDegree;
	ArrayList<Integer> chunks = new ArrayList<Integer>();

	public BackedUpFile(String pathname, String fileId, byte desiredRepDegree) {
		this.pathname = pathname;
		this.fileId = fileId;
		this.desiredRepDegree = desiredRepDegree;
	}

	public void present() {
		System.out.format("- %s | %s | %d\n", pathname, fileId, desiredRepDegree);
//		for (final Chunk chunk : chunks) {
//			chunk.present();
//		}
	}

	public String getFileId() {
		return fileId;
	}

	public void initChunks(int chunkNo) {
		chunks.clear();
		for (int i = 0; i < chunkNo; i++) {
			chunks.add(i);
		}
	}
	
}
