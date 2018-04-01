import java.net.InetAddress;

public class Config {

    private static String peerId;

    private static InetAddress mcIP;
    private static int mcPort;

    private static InetAddress mdbIP;
    private static int mdbPort;

    private static InetAddress mdrIP;
    private static int mdrPort;

    private static String peerDir;

    public static int MAX_CHUNK_SIZE = 64000; //bytes

    public static void setPeer(String peerId){
    	Config.peerDir = String.format("Peer_%s/", peerId);
    	Config.peerId = peerId;
	}

	/**
	 * @return the peerId
	 */
	public static String getPeerId() {
		return peerId;
	}


	/**
	 * @return the mcPort
	 */
	public static int getMcPort() {
		return mcPort;
	}
	/**
	 * @param mcPort the mcPort to set
	 */
	public static void setMcPort(int mcPort) {
		Config.mcPort = mcPort;
	}
	/**
	 * @return the mdbIP
	 */
	public static InetAddress getMdbIP() {
		return mdbIP;
	}
	/**
	 * @param mdbIP the mdbIP to set
	 */
	public static void setMdbIP(InetAddress mdbIP) {
		Config.mdbIP = mdbIP;
	}
	/**
	 * @return the mdbPort
	 */
	public static int getMdbPort() {
		return mdbPort;
	}
	/**
	 * @param mdbPort the mdbPort to set
	 */
	public static void setMdbPort(int mdbPort) {
		Config.mdbPort = mdbPort;
	}

	/**
	 * @return the mdrIP
	 */
	public static InetAddress getMdrIP() {
		return mdrIP;
	}
	/**
	 * @param mdrIP the mdrIP to set
	 */
	public static void setMdrIP(InetAddress mdrIP) { Config.mdrIP = mdrIP;
	}
	/**
	 * @return the mdrPort
	 */
	public static int getMdrPort() {
		return mdrPort;
	}
	/**
	 * @param mdrPort the mdrPort to set
	 */
	public static void setMdrPort(int mdrPort) {
		Config.mdrPort = mdrPort;
	}

	/**
	 * @return the mcIP
	 */
	public static InetAddress getMcIP() {
		return mcIP;
	}

	/**
	 * @param mcIP the mcIP to set
	 */
	public static void setMcIP(InetAddress mcIP) {
		Config.mcIP = mcIP;
	}

    /**
     * @return the peer Directory where his data will be saved
     */
    public static String getPeerDir() {
        return peerDir;
    }
}