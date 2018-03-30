import java.rmi.Remote;

public interface RMIInterface extends Remote{
	public void backup(String pathname) throws java.rmi.RemoteException;
	public void restore(String pathname) throws java.rmi.RemoteException;
	public void delete(String pathname) throws java.rmi.RemoteException;
	public void state() throws java.rmi.RemoteException;
	public void reclaim(int maxDiskSpace) throws java.rmi.RemoteException;
}
