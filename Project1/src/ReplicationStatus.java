import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import utils.Pair;
import utils.ThreadUtils;

/**
 * Association of (fileId, chunkNo) pair with actual and desired replication degrees.
 * Desired = Integer read from PUTCHUNKs in MDB.
 * Actual = Set of Peer ID's which sent confirmations (STORED) in MC.
 */
public class ReplicationStatus implements Serializable {
	private static final long serialVersionUID = -6171144910000784686L;
	
	private ConcurrentHashMap<Pair<String, Integer>, Pair<Byte, HashSet<String>>> repDegrees;
	private transient ObjectOutputStream out;
	private AtomicLong bytesUsed;
	private AtomicLong bytesReserved;
	
	public ReplicationStatus(String path) {
		repDegrees = new ConcurrentHashMap<>();
	    setOutputStream(path);
	    bytesUsed = new AtomicLong(0);
	    bytesReserved = new AtomicLong(Long.MAX_VALUE);
	}

	public Map<Pair<String,Integer>, Pair<Byte, HashSet<String>>> getSortedMap(){
        return repDegrees.entrySet().stream()
                        .sorted(Entry.comparingByValue())
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
    }


	public void decrementLocalCount(String senderId, String fileId, int chunkNo){
		repDegrees.get(new Pair(fileId,chunkNo)).getRight().remove(senderId);
	}
	public Byte getDesiredReplicationDeg(String fileId, Integer chunkNo){
	    return repDegrees.get(new Pair(fileId,chunkNo)).getLeft();
    }

	public int getNumConfirms(String fileId, int chunkNo) {
		Pair<Byte, HashSet<String>> pair = repDegrees.get(new Pair<>(fileId, chunkNo));
		if (pair == null) return 0;
		HashSet<String> peerIds = pair.getRight();
		if (peerIds == null) {
			return 0;
		}
		return peerIds.size();
	}

	public void putchunk_setDesiredReplicationDeg(byte repDeg, String fileId, Integer chunkNo) {
	    System.out.println("ReplicationStatus: Saving desired replication degree for " + chunkNo);
		Pair<String, Integer> key = new Pair<>(fileId, chunkNo);
		repDegrees.putIfAbsent(key, new Pair<>(repDeg, new HashSet<>()));
		Pair<Byte, HashSet<String>> entry = repDegrees.get(key);
		entry.setLeft(repDeg);
		tryToWrite();

	}
	
	public void stored_addPeerId(String peerId, String fileId, Integer chunkNo) {
		Pair<String, Integer> key = new Pair<>(fileId, chunkNo);
		repDegrees.putIfAbsent(key, new Pair<>((byte)0, new HashSet<>()));
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

    public Long getBytesUsed() {
        return bytesUsed.get();
    }

    public void incrementBytesUsed(long bytesToAdd) {
		bytesUsed.addAndGet(bytesToAdd);
    }

    public void decrementBytesUsed(long bytesToSubtract){
	    System.out.println("Decrementing by: " + bytesToSubtract);
	    bytesUsed.addAndGet(-bytesToSubtract);
    }

    public Long getBytesReserved() {
        return bytesReserved.get();
    }

    public void setBytesReserved(Long bytesReserved) {
        this.bytesReserved.set(bytesReserved);
    }
}
