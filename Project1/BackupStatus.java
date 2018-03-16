import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import utils.Pair;

// Association of (fileId, chunkNo) pair with set of Peer ID's which sent confirmations on MC.
public class BackupStatus {
	private ConcurrentHashMap<Pair<String, Integer>, HashSet<String>> storeConfirmations = new ConcurrentHashMap<Pair<String, Integer>, HashSet<String>>();

	public int getNumConfirms(String fileId, int chunkNo) {
		Set<String> peerIds = storeConfirmations.get(new Pair<String, Integer>(fileId, chunkNo));
		if (peerIds == null) {
			return 0;
		}
		return peerIds.size();
	}

	public void addPeerId(String peerId, String fileId, Integer chunkNo) {
		Pair<String, Integer> key = new Pair<String, Integer>(fileId, chunkNo);
		storeConfirmations.putIfAbsent(key, new HashSet<String>());
		Set<String> peerIds = storeConfirmations.get(key);
		peerIds.add(peerId);
	}
	
}
