import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ReplicationStatusFactory {

	public static ReplicationStatus getNew(String peerDir) {
		String path = peerDir + "data/ReplicationStatus.ser";
		ObjectInputStream objectinputstream = null;
		try {
		    FileInputStream streamIn = new FileInputStream(path);
		    objectinputstream = new ObjectInputStream(streamIn);
		    return ((ReplicationStatus) objectinputstream.readObject()).setOutputStream(path);
		} catch (EOFException e) { // file does not contain serialized object to load
			return new ReplicationStatus(path);
		} catch (FileNotFoundException e) { // no old file found
			return new ReplicationStatus(path);
		} catch (Exception e) { // unexpected exception
		    System.out.println("Corrupted Replication Status, creating new");
		    FileProcessor.deleteFile(path);
		    return new ReplicationStatus(path);
		} finally {
		    if(objectinputstream != null){
		        try {
					objectinputstream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    } 
		}
	}

}
