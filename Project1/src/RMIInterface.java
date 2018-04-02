import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote{
	public void backup(String pathname, byte desiredRepDegree) throws java.rmi.RemoteException;
	public void backupEnh(String pathname, byte desiredRepDegree) throws java.rmi.RemoteException;
	public void restore(String pathname) throws java.rmi.RemoteException;
	public void delete(String pathname) throws java.rmi.RemoteException;
	public PeerState state() throws java.rmi.RemoteException;
	public void reclaim(long maxDiskSpace) throws java.rmi.RemoteException;
	public void restoreEnh(String pathname) throws java.rmi.RemoteException;
}