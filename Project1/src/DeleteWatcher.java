
public class DeleteWatcher implements Runnable {

	private ReplicationStatus repStatus;

	public DeleteWatcher(ReplicationStatus repStatus) {
		this.repStatus = repStatus;
	}

	@Override
	public void run() {
		repStatus.checkFilesToDelete();
	}

}
