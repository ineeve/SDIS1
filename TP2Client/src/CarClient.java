import java.io.IOException;
import java.net.*;

public class CarClient {

	private static InetAddress mcast_addr;
	private static int mcast_port;
	private static InetAddress srvc_addr;
	private static int srvc_port;
	private static String operation, plateNumber, owner;
	private static MulticastSocket mcast_socket;
	private static DatagramSocket clientSocket;
	
	private static void parseArgs(String[] args) throws UnknownHostException {
		if (args.length < 4) {
			System.out.println("Error args");
			System.out.println("java CarClient <mcast_addr> <mcast_port> <oper> <opnd>*");
			System.exit(0);
		}
		mcast_addr = InetAddress.getByName(args[0]);
		mcast_port = Integer.parseInt(args[1]);
		operation = args[2];
		plateNumber = args[3];
		if (args.length == 5){
			owner = args[4];
		}
	}
	
	private static DatagramPacket createSendPacket() {
		String msgStr = operation + " " + plateNumber;
		if (owner != null) {
			msgStr += " " + owner;
		}
		System.out.println("Client sending: " + msgStr);
		byte[] msg = msgStr.getBytes();
		return new DatagramPacket(msg, msg.length, srvc_addr, srvc_port);
	}
	
	private static DatagramPacket createReplyPacket() {
		byte[] replyMsg = new byte[512];
		return new DatagramPacket(replyMsg, replyMsg.length);
	}
	
	private static void setupConnection() throws IOException {
		mcast_socket = new MulticastSocket(mcast_port);
		mcast_socket.joinGroup(mcast_addr);
		
		setupUnicastConnection();
	}
	
	private static void setupUnicastConnection() throws IOException {
		byte[] msg = new byte[512];
		DatagramPacket multicastPacket = new DatagramPacket(msg, msg.length);
		mcast_socket.receive(multicastPacket);
		
		String[] multicastReply = new String(multicastPacket.getData()).trim().split(" ");
		srvc_addr = InetAddress.getByName(multicastReply[0]);
		srvc_port = Integer.parseInt(multicastReply[1]);
		clientSocket = new DatagramSocket();
	}

	public static void main(String[] args) throws IOException {
		
		DatagramPacket sendPacket, replyPacket;
		
		parseArgs(args);
		setupConnection();
		sendPacket = createSendPacket();
		replyPacket = createReplyPacket();
		
		clientSocket.send(sendPacket);
		clientSocket.receive(replyPacket);
		
		String msgStr = new String(sendPacket.getData()).trim();
		String replyStr = new String(replyPacket.getData()).trim();
		System.out.println(msgStr + ": " + replyStr);		
	}

}
