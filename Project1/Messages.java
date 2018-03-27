import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class Messages {

	private final static String STORED = "STORED";
	private final static String GETCHUNK = "GETCHUNK";
	
	
	public static boolean isStored(DatagramPacket packet) {
		return getType(packet) == STORED;
	}

	public static boolean isGetChunk(DatagramPacket packet) {
		return getType(packet) == GETCHUNK;
	}

	private static String getType(DatagramPacket packet) {
		String msg = new String(packet.getData(), Charset.forName("ISO_8859_1")).trim();
		return msg.split(" ")[0];
	}

}
