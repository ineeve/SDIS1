import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class Messages {

	private final static String CRLF = "\r\n";

	private final static String STORED = "STORED";
	private final static String GETCHUNK = "GETCHUNK";
	private final static String CHUNK = "CHUNK";
	private final static String PUTCHUNK = "PUTCHUNK";
	private final static String REMOVED = "REMOVED";
	private final static String DELETE = "DELETE";

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
	public static boolean isRemoved(DatagramPacket packet){ return getType(packet).equals(REMOVED); }

	public static boolean isDelete(DatagramPacket packet) {
		return getType(packet).equals(DELETE);
	}

	private static String getType(DatagramPacket packet) {
		String msg = new String(packet.getData(), Charset.forName("ISO_8859_1")).trim();
		return msg.split(" ")[0];
	}

	private static byte[] getBytesFromString(String str){
	    return str.getBytes(Charset.forName("ISO_8859_1"));
    }

	public static byte[] getPUTCHUNKHeader(String version, String fileId, Integer chunkNo, Byte repDeg){
		return getBytesFromString(String.format("PUTCHUNK %s %s %s %d %d %s%s", version, Config.getPeerId(), fileId, chunkNo, repDeg, CRLF, CRLF));
	}
	public static byte[] getDELETEHeader(String fileId){
		return getBytesFromString(String.format("DELETE %s %s %s %s%s",Config.ORIG_VERSION, Config.getPeerId(), fileId, CRLF, CRLF));
	}
	public static byte[] getREMOVEDHeader(String fileId, Integer chunkNo){
	    return getBytesFromString(String.format("REMOVED %s %s %s %s %s%s",Config.ORIG_VERSION, Config.getPeerId(), fileId, chunkNo, CRLF, CRLF));
    }

    public static byte[] getChunkHeader(String fileId, Integer chunkNo){
		return getBytesFromString(String.format("CHUNK %s %s %s %d %s%s",Config.ORIG_VERSION, Config.getPeerId(), fileId, chunkNo, CRLF, CRLF));
	}
	public static byte[] getGETCHUNKHeader(String fileId, Integer chunkNo){
		return getBytesFromString(String.format("GETCHUNK %s %s %s %s %s%s",Config.ORIG_VERSION, Config.getPeerId(), fileId, chunkNo, CRLF, CRLF));
	}
	
	public static byte[] getDELETEDHeader(String fileId){
		return getBytesFromString(String.format("DELETED %s %s %s %s%s", Config.ENH_VERSION, Config.getPeerId(), fileId, CRLF, CRLF));
	}
}

