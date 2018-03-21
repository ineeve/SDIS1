import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import utils.Pair;
	
// Association of (fileId, chunkNo) pair with set of Peer ID's which sent confirmations on MC.
public class BackupStatus implements Serializable {
	private static final long serialVersionUID = 8221399383298899531L;

	private ConcurrentHashMap<Pair<String, Integer>, HashSet<String>> storeConfirmations = new ConcurrentHashMap<Pair<String, Integer>, HashSet<String>>();
	private transient ObjectOutputStream out;
	
	public BackupStatus() {
		try {
			FileOutputStream fileOut = new FileOutputStream("data/BackupStatus.ser");
			out = new ObjectOutputStream(fileOut);
		} catch (IOException e) {
			System.out.println("WARNING: Failed to open storage of received confirmations. Received confirmations will be disposed.");
			e.printStackTrace();
		}
        
        System.out.println("Serialized data is saved in data/BackupStatus.ser");
	}
	
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
		try {
			out.writeObject(this);
		} catch (IOException e) {
			System.out.println("Failed to write storage of received confirmations.");
			e.printStackTrace();
		}
	}
	
}
