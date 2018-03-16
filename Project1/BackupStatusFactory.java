import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class BackupStatusFactory {

	public static BackupStatus getNew() {
		ObjectInputStream objectinputstream = null;
		try {
		    FileInputStream streamIn = new FileInputStream("data/BackupStatus.ser");
		    objectinputstream = new ObjectInputStream(streamIn);
		    BackupStatus readStatus = (BackupStatus) objectinputstream.readObject();
		    if (readStatus != null) {
		    	return readStatus;
		    } else {
		    	return new BackupStatus();
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    return new BackupStatus();
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
