import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ReplicationStatusFactory {

	public static ReplicationStatus getNew() {
		ObjectInputStream objectinputstream = null;
		try {
		    FileInputStream streamIn = new FileInputStream("data/ReplicationStatus.ser");
		    objectinputstream = new ObjectInputStream(streamIn);
		    return ((ReplicationStatus) objectinputstream.readObject()).setOutputStream();
		} catch (EOFException e) { // file does not contain serialized object to load
			return new ReplicationStatus();
		} catch (FileNotFoundException e) { // no old file found
			return new ReplicationStatus();
		} catch (Exception e) { // unexpected exception
		    e.printStackTrace();
		    return new ReplicationStatus();
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
