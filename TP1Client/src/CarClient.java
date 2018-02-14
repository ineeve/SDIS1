import java.io.IOException;
import java.net.*;

public class CarClient {

	private static int port;
	private static String host;
	private static String operation, plateNumber, owner;
	
	private static void parseArgs(String[] args) {
		if (args.length < 4) {
			System.out.println("Error no args");
			System.out.println("java CarClient <host_name> <server_port> <oper> <opnd>*");
			System.exit(0);
		}
		host = args[0];
		port = Integer.parseInt(args[1]);
		operation = args[2];
		plateNumber = args[3];
		if (args.length == 5){
			owner = args[4];
		}
	}
	
	public static void main(String[] args) {
		
		DatagramSocket clientSocket;
		DatagramPacket sendPacket, replyPacket;
		InetAddress serverAddress;
		
		parseArgs(args);
		String msgStr = operation + " " + plateNumber;
		if (owner != null) {
			msgStr += " " + owner;
		}
		System.out.println("Client Sending: " + msgStr);
		byte[] msg = msgStr.getBytes();
		
		try {
			byte[] replyMsg = new byte[512];
			
			serverAddress = Inet4Address.getByName(host);
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(1000);
			
			sendPacket = new DatagramPacket(msg, msg.length, serverAddress, port);
			replyPacket = new DatagramPacket(replyMsg, replyMsg.length);
			
			clientSocket.send(sendPacket);
			clientSocket.receive(replyPacket);
			
			String replyStr = new String(replyPacket.getData()).trim();
			
			System.out.print(msgStr + ": " + replyStr);
		} catch (SocketException e) {
			//System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
