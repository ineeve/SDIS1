import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteWatcher implements Runnable {

	private ExecutorService pool = Executors.newCachedThreadPool();
	private MulticastSocket mcSocket;
	private ReplicationStatus repStatus;

	public DeleteWatcher(MulticastSocket mcSocket, ReplicationStatus repStatus) {
		this.mcSocket = mcSocket;
		this.repStatus = repStatus;
	}

	@Override
	public void run() {
		ArrayList<String> filesToDelete = repStatus.getFilesToDelete();
		for (String fileId : filesToDelete) {
			pool.execute(new SendDeleteFile(Config.getCurrentVersion(), mcSocket, fileId, repStatus));
		}
	}

}
