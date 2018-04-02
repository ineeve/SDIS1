import java.net.InetAddress;

public class Config {

	public static final String MIN_MULTICAST_IP = "224.0.0.1";
	public static final String MAX_MULTICAST_IP = "239.255.255.255";
	public static final String ORIG_VERSION = "1.0";
	public static final String ENH_VERSION = "2.0";
	
	private static String version;
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

	public static void checkIPRanges() throws Exception {
		long minIp = ipToLong(InetAddress.getByName(MIN_MULTICAST_IP));
		long maxIp = ipToLong(InetAddress.getByName(MAX_MULTICAST_IP));
		
		long mdbIpLong = ipToLong(mdbIP);
		long mcIpLong = ipToLong(mcIP);
		long mdrIpLong = ipToLong(mdrIP);
		if (!(mdbIpLong >= minIp && mdbIpLong <= maxIp
				&& mcIpLong >= minIp && mcIpLong <= maxIp
				&& mdrIpLong >= minIp && mdrIpLong <= maxIp)) {
			throw new Exception("Multicast IP not in valid range.");
		}
	}
	
	private static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }
	
	public static void setCurrentVersion(String version) {
		Config.version = version;
	}
	
	public static String getCurrentVersion() {
		return version;
	}

	public static boolean isEnhanced() {
		return version.equals(ENH_VERSION);
	}
}