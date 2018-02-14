import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class CarServer {

	private static int port;
	private static DatagramSocket serverSocket;
	private static DatagramPacket receivedPacket;
	private static DatagramPacket sendPacket;
	private static HashMap<String,String> storedData = new HashMap<String,String>();
	
	private static void parseArgs(String[] args){
		if (args.length < 1) {
			System.out.println("Error no args");
			System.out.println("java CarServer <server_port>");
			System.exit(0);
		}
		port = Integer.parseInt(args[0]);
	}
	
	private static void setupConnection() {
		try {
			
			serverSocket = new DatagramSocket(port);
			System.out.println("Server listening on port " + port);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
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

	public static void main(String[] args) {
		parseArgs(args);
		setupConnection();
		while (true) {
			serverIteration();
		}
	}

}
