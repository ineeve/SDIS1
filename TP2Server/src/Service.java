import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Service implements Runnable {

	private HashMap<String,String> storedData = new HashMap<String,String>();
	private int srvc_port;
	private InetAddress serviceAddress;
	private DatagramSocket serverSocket;
	private DatagramPacket receivedPacket;
	private DatagramPacket sendPacket;

	public Service(int srvc_port) {
		this.srvc_port = srvc_port;
	}
	
	public void run() {
		try {
			setupConnection();
			while (true) {
				serverIteration();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public InetAddress getAddress() {
		return serviceAddress;
	}
	
	private void setupConnection() throws IOException {
		serviceAddress = InetAddress.getByName("localhost");
		serverSocket = new DatagramSocket(srvc_port, serviceAddress);
		System.out.println("Server listening on port " + srvc_port);

}
	
	private void serverIteration() {
		try {
			byte[] receivedMsg = new byte[512];
			receivedPacket = new DatagramPacket(receivedMsg, receivedMsg.length);
			serverSocket.receive(receivedPacket);
			InetAddress clientAddr = receivedPacket.getAddress();
			int clientPort = receivedPacket.getPort();
			String replyMsg = parseClientPacket(receivedPacket);
			byte[] replyBytes = replyMsg.getBytes();
			sendPacket = new DatagramPacket(replyBytes, replyBytes.length, clientAddr, clientPort);
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	private String parseClientPacket(DatagramPacket receivedPacket) {
		String receivedStr = new String(receivedPacket.getData()).trim();
		String[] arguments = receivedStr.split(" ");

		if (arguments[0].equals("register")){
			storedData.put(arguments[1],arguments[2]);
			return Integer.toString(storedData.size());
		}else if (arguments[0].equals("lookup")){
			String ownerName = storedData.get(arguments[1]);
			if (ownerName != null){
				return arguments[1] + " " + ownerName;
			}
			return "plate number is not registered";
		}else{
			System.out.println("Invalid operation: " + arguments[0]);
			return "-1";
		}
	}

}
