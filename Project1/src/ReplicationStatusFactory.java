import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ReplicationStatusFactory {

	public static ReplicationStatus getNew(String peerId) {
		String path = "data/ReplicationStatus_" + peerId + ".ser";
		ObjectInputStream objectinputstream = null;
		try {
		    FileInputStream streamIn = new FileInputStream(path);
		    objectinputstream = new ObjectInputStream(streamIn);
		    return ((ReplicationStatus) objectinputstream.readObject()).setOutputStream(peerId);
		} catch (EOFException e) { // file does not contain serialized object to load
			return new ReplicationStatus(peerId);
		} catch (FileNotFoundException e) { // no old file found
			return new ReplicationStatus(peerId);
		} catch (Exception e) { // unexpected exception
		    e.printStackTrace();
		    return new ReplicationStatus(peerId);
		} finally {
		    if(objectinputstream != null){
		        try {
					objectinputstream .close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    } 
		}
	}

}
