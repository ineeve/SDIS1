import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.TimerTask;

public class MulticastSendTask extends TimerTask {
	
	private MulticastSocket socket;
	private DatagramPacket packet;

	public MulticastSendTask(MulticastSocket socket, DatagramPacket packet) {
		this.socket = socket;
		this.packet = packet;
	}
	
	@Override
	public void run() {
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
