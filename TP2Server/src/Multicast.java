import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Multicast implements Runnable {

	private InetAddress mcast_addr;
	private int mcast_port;
	private MulticastSocket mcast_socket;
	
	public Multicast(InetAddress mcast_addr, int mcast_port) {
		
	}
	
	private void setupConnection() throws IOException {
		mcast_socket = new MulticastSocket(mcast_port);
		mcast_socket.joinGroup(mcast_addr);
		System.out.println("Server in multicast group " + mcast_addr + mcast_port);

}
	
	public void run() {
		try {
			setupConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
