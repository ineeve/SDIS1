import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import utils.Pair;
	
/**
 * Association of (fileId, chunkNo) pair with actual and desired replication degrees.
 * Desired = Integer read from PUTCHUNKs in MDB.
 * Actual = Set of Peer ID's which sent confirmations (STORED) in MC.
 */
public class ReplicationStatus implements Serializable {
	private static final long serialVersionUID = -6171144910000784686L;
	
	private ConcurrentHashMap<Pair<String, Integer>, Pair<Integer, HashSet<String>>> repDegrees
				= new ConcurrentHashMap<Pair<String, Integer>, Pair<Integer, HashSet<String>>>();
	private transient ObjectOutputStream out;
	
	public ReplicationStatus() {
		setOutputStream();
	}
	
	public int getNumConfirms(String fileId, int chunkNo) {
		HashSet<String> peerIds = repDegrees.get(new Pair<String, Integer>(fileId, chunkNo)).getRight();
		if (peerIds == null) {
			return 0;
		}
		return peerIds.size();
	}

	public void putchunk_setDesiredReplicationDeg(int repDeg, String fileId, Integer chunkNo) {
		Pair<String, Integer> key = new Pair<String, Integer>(fileId, chunkNo);
		repDegrees.putIfAbsent(key, new Pair<Integer, HashSet<String>>(repDeg, new HashSet<String>()));
		Pair<Integer, HashSet<String>> entry = repDegrees.get(key);
		entry.setLeft(repDeg);
		try {
			out.writeObject(this);
		} catch (IOException e) {
			System.out.println("Failed to write storage of desired replication degree.");
			e.printStackTrace();
		}
	}
	
	public void stored_addPeerId(String peerId, String fileId, Integer chunkNo) {
		Pair<String, Integer> key = new Pair<String, Integer>(fileId, chunkNo);
		repDegrees.putIfAbsent(key, new Pair<Integer, HashSet<String>>(0, new HashSet<String>()));
		HashSet<String> peerIds = repDegrees.get(key).getRight();
		peerIds.add(peerId);
		try {
			out.writeObject(this);
		} catch (IOException e) {
			System.out.println("Failed to write storage of received confirmations.");
			e.printStackTrace();
		}
	}

	public ReplicationStatus setOutputStream() {
		try {
			FileOutputStream fileOut = new FileOutputStream("data/ReplicationStatus.ser");
			out = new ObjectOutputStream(fileOut);
		} catch (IOException e) {
			System.out.println("WARNING: Failed to open storage of replication degree data. Received replication degree data will be disposed.");
			e.printStackTrace();
		}
        System.out.println("Serialized data is saved in data/ReplicationStatus.ser");
		return this;
	}
	
}
