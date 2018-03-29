import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import utils.Pair;
import utils.ThreadUtils;

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
	
	public ReplicationStatus(String path) {
		setOutputStream(path);
	}
	
	public int getNumConfirms(String fileId, int chunkNo) {
		Pair<Integer,HashSet<String>> pair = repDegrees.get(new Pair<String, Integer>(fileId, chunkNo));
		if (pair == null) return 0;
		HashSet<String> peerIds = pair.getRight();
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
		tryToWrite();

	}
	
	public void stored_addPeerId(String peerId, String fileId, Integer chunkNo) {
		Pair<String, Integer> key = new Pair<String, Integer>(fileId, chunkNo);
		repDegrees.putIfAbsent(key, new Pair<Integer, HashSet<String>>(0, new HashSet<String>()));
		HashSet<String> peerIds = repDegrees.get(key).getRight();
		peerIds.add(peerId);
		tryToWrite();
	}

	public ReplicationStatus setOutputStream(String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			out = new ObjectOutputStream(fileOut);
		} catch (IOException e) {
			System.out.println("WARNING: Failed to open storage of replication degree data. Received replication degree data will be disposed.");
			e.printStackTrace();
		}
        System.out.println("Serialized data is saved in " + path);
		return this;
	}

	private void tryToWrite(){
		boolean wasWritten = false;
		do{
			try {
				out.writeObject(this);
                wasWritten = true;
			} catch (IOException e) {
				System.out.println("Failed to write Replication Status.");
                ThreadUtils.waitBetween(10,400);
			}
		}while(!wasWritten);
	}
	
}
