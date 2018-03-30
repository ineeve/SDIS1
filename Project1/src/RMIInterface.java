import java.rmi.Remote;

public interface RMIInterface extends Remote{
	public void backup() throws java.rmi.RemoteException;
	public void restore() throws java.rmi.RemoteException;
	public void delete() throws java.rmi.RemoteException;
	public void state() throws java.rmi.RemoteException;
}
