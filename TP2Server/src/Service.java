import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Service implements Runnable {

	// (plate number, owner name)
	private HashMap<String,String> storedData = new HashMap<String,String>();
	private int srvc_port;
	private InetAddress serviceAddress;
	private DatagramSocket serverSocket;
	private DatagramPacket receivedPacket;
	private DatagramPacket sendPacket;

	public Service(int srvc_port) throws IOException {
		this.srvc_port = srvc_port;
		setupConnection();
	}
	
	public void run() {
		while (true) {
			serverIteration();
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
		}
	}
	
	private String parseClientPacket(DatagramPacket receivedPacket) {
		String receivedStr = new String(receivedPacket.getData()).trim();
		String[] arguments = receivedStr.split(" ");
		String plateNumber, ownerName;
		
		if (arguments[0].equals("register")) {
			plateNumber = arguments[1];
			ownerName = arguments[2];
			storedData.put(plateNumber, ownerName);
			System.out.println("register " + plateNumber + " " + ownerName + " :: " + Integer.toString(storedData.size()));
			return Integer.toString(storedData.size());
		} else if (arguments[0].equals("lookup")) {
			plateNumber = arguments[1];
			ownerName = storedData.get(plateNumber);
			
			if (ownerName != null) {
				String result = plateNumber + " " + ownerName;
				System.out.println("lookup " + plateNumber + " :: " + result);
				return result;
			}
			
			String result = "plate number is not registered";
			System.out.println("lookup " + plateNumber + " :: " + result);
			return result;
		} else {
			System.out.println("Invalid operation: " + arguments[0]);
			return "-1";
		}
	}

}
