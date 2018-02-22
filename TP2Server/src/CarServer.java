import java.io.IOException;
import java.net.*;
import java.util.Timer;

public class CarServer {

	private static int srvc_port;
	private static InetAddress mcast_addr;
	private static int mcast_port;
	
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
		

	public static void main(String[] args) throws IOException {
		parseArgs(args);
		Service carService = new Service(srvc_port);
		Multicast multicast = new Multicast(mcast_addr, mcast_port, carService.getAddress(), srvc_port);

		Thread serviceThread = new Thread(carService);
		Timer multicastTimer = new Timer();
		
		serviceThread.start();
		multicastTimer.schedule(multicast, 0, 1000);
	}

}
