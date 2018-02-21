import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Timer;


public class Multicast implements Runnable {
	private int srvc_port;
	private InetAddress srvc_addr;
	private InetAddress mcast_addr;
	private int mcast_port;
	private MulticastSocket mcast_socket;
	
	public Multicast(InetAddress mcast_addr, int mcast_port, InetAddress srvc_addr, int srvc_port) {
		this.srvc_port = srvc_port;
		this.srvc_addr = srvc_addr;
		this.mcast_addr = mcast_addr;
		this.mcast_port = mcast_port;
	}
	
	private void setupConnection() throws IOException {
		mcast_socket = new MulticastSocket(mcast_port);
		mcast_socket.joinGroup(mcast_addr);
		System.out.println("Server in multicast group " + mcast_addr + mcast_port);
	}
	
	private byte[] createMessage() {
		String message = "" + srvc_addr + srvc_port;
		return message.getBytes();
	}
	
	public void run() {
		try {
			setupConnection();
			byte[] message = createMessage();
			DatagramPacket packet = new DatagramPacket(
					message, 
					message.length, 
					mcast_addr, 
					mcast_port);
			
			Timer timer = new Timer();
			MulticastSendTask multicastSendTask = new MulticastSendTask(mcast_socket, packet);
			timer.schedule(multicastSendTask, 1000);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
