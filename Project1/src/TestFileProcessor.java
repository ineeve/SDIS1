import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import utils.FutureBuffer;

/**
 * Class used merely to test asynchronous read and write operations.
 */
public class TestFileProcessor {

	public static void main(String[] args) {
		// read
		File file = FileProcessor.loadFile(args[0]);
		ArrayList<FutureBuffer> futures = FileProcessor.readFileChunksAsync(file);
		ArrayList<byte[]> bodies = new ArrayList<>();
		for (FutureBuffer future : futures) {
			byte[] body = FileProcessor.getDataFromFuture(future);
			bodies.add(body);
		}
		
		// write
		Path path = Paths.get("new/" + args[0]);
		FileProcessor.writeFileAsync(path, bodies);
	}

}
