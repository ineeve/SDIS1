import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import utils.ThreadUtils;

public class PeerState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7200331457829652271L;
	private long maxDiskSpace; //KBytes
	private long usedDiskSpace; //KBytes
	
	public PeerState(ReplicationStatus repStatus) {
		maxDiskSpace = repStatus.getBytesReserved() / 1000;
		usedDiskSpace = repStatus.getBytesUsed() / 1000;
	}

	public void present() {
		System.out.format("Used disk space: %d KB\n", usedDiskSpace);
		System.out.format("Maximum disk space: %d KB\n", maxDiskSpace);
	}
}
