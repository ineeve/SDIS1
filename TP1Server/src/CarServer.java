import java.io.IOException;
import java.net.*;

public class CarServer {

	private static int port;
	private static DatagramSocket serverSocket;
	private static DatagramPacket receivePacket;
	private static DatagramPacket sendPacket;
	
	private static void parseArgs(String[] args){
		if (args.length < 1) {
			System.out.println("Error no args");
			System.exit(0);
		}
		port = Integer.parseInt(args[0]);
	}
	
	private static void setupConnection() {
		try {
			byte[] receiveMsg = new byte[1024];
			serverSocket = new DatagramSocket(port);
			receivePacket = new DatagramPacket(receiveMsg, receiveMsg.length);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	
	private static void serverIteration() {
		try {
			serverSocket.receive(receivePacket);
			InetAddress clientAddr = receivePacket.getAddress();
			int clientPort = receivePacket.getPort();
			
			String operation, plateNumber, ownerName;
			parseClientPacket(receivePacket, operation, plateNumber, ownerName);
			
			sendPacket.setAddress(clientAddr);
			sendPacket.setPort(clientPort);
			sendPacket.setData(sendMsg);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	private static void parseClientPacket(DatagramPacket receivePacket,
			String operation, String plateNumber, String ownerName) {
				
	}

	public static void main(String[] args) {
		parseArgs(args);
		setupConnection();
		while (true) {
			serverIteration();
		}
	}

}
