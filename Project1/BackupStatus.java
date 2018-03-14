import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import utils.Pair;

// Association of (fileId, chunkNo) pair with set of Peer ID's which sent confirmations on MC.
public class BackupStatus {
	private ConcurrentHashMap<Pair<String, Integer>, Set<String>> storeConfirmations;

	public int getNumConfirms(String fileId, int chunkNo) {
		return storeConfirmations.get(new Pair<String, Integer>(fileId, chunkNo)).size();
	}
	
}
