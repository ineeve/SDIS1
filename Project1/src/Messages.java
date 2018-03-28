import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class Messages {

	private final static String STORED = "STORED";
	private final static String GETCHUNK = "GETCHUNK";
	private final static String CHUNK = "CHUNK";
	private final static String PUTCHUNK = "PUTCHUNK";
	
	
	public static boolean isStored(DatagramPacket packet) {
		return getType(packet).equals(STORED);
	}

	public static boolean isGetChunk(DatagramPacket packet) {
		return getType(packet).equals(GETCHUNK);
	}
	public static boolean isChunk(DatagramPacket packet){
		return getType(packet).equals(CHUNK);
	}
	public static boolean isPutChunk(DatagramPacket packet){
		return getType(packet).equals(PUTCHUNK);
	}

	private static String getType(DatagramPacket packet) {
		String msg = new String(packet.getData(), Charset.forName("ISO_8859_1")).trim();
		return msg.split(" ")[0];
	}

}
