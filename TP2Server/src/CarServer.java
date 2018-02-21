import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class CarServer {

	private static int srvc_port;
	private static InetAddress mcast_addr;
	private static int mcast_port;
	private static MulticastSocket serverSocket;
	private static DatagramPacket receivedPacket;
	private static DatagramPacket sendPacket;
	private static HashMap<String,String> storedData = new HashMap<String,String>();
	
	private static void parseArgs(String[] args) throws UnknownHostException {
		if (args.length != 3) {
			System.out.println("Error args");
			System.out.println("java CarServer <srvc_port> <mcast_addr> <mcast_port>");
			System.exit(0);
		}
		srvc_port = Integer.parseInt(args[0]);
		mcast_addr = InetAddress.getByName(args[1]);
		mcast_port = Integer.parseInt(args[2]);
		
	}
	
	private static void setupConnection() throws IOException {
			serverSocket = new MulticastSocket(mcast_port);
			serverSocket.joinGroup(mcast_addr);
			System.out.println("Server listening on port " + srvc_port);

	}
	
	private static void serverIteration() {
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
	
	private static String parseClientPacket(DatagramPacket receivedPacket) {
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

	public static void main(String[] args) throws IOException {
		parseArgs(args);
		
		Thread multicastThread = new Thread(new Multicast());
		Thread serviceThread = new Thread(new Service());
		multicastThread.start();
		serviceThread.start();
	}

}
