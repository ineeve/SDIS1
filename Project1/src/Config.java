import java.net.InetAddress;

public class Config {

    private String peerId;
    private String protocolVersion;

    private InetAddress mcIP;
    private int mcPort;

    private InetAddress mdbIP;
    private int mdbPort;

    private InetAddress mdrIP;
    private int mdrPort;

    private String peerDir;

    public static int MAX_CHUNK_SIZE = 64000; //bytes

    public Config(String peerId, String protocol){
        this.peerId = peerId;
        this.peerDir = String.format("Peer_%s/", peerId);
        this.protocolVersion = protocol;
    }


	/**
	 * @return the peerId
	 */
	public String getPeerId() {
		return peerId;
	}

	/**
	 * @return the protocolVersion
	 */
	public String getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * @return the mcPort
	 */
	public int getMcPort() {
		return mcPort;
	}
	/**
	 * @param mcPort the mcPort to set
	 */
	public void setMcPort(int mcPort) {
		this.mcPort = mcPort;
	}
	/**
	 * @return the mdbIP
	 */
	public InetAddress getMdbIP() {
		return mdbIP;
	}
	/**
	 * @param mdbIP the mdbIP to set
	 */
	public void setMdbIP(InetAddress mdbIP) {
		this.mdbIP = mdbIP;
	}
	/**
	 * @return the mdbPort
	 */
	public int getMdbPort() {
		return mdbPort;
	}
	/**
	 * @param mdbPort the mdbPort to set
	 */
	public void setMdbPort(int mdbPort) {
		this.mdbPort = mdbPort;
	}

	/**
	 * @return the mdrIP
	 */
	public InetAddress getMdrIP() {
		return mdrIP;
	}
	/**
	 * @param mdrIP the mdrIP to set
	 */
	public void setMdrIP(InetAddress mdrIP) {
		this.mdrIP = mdrIP;
	}
	/**
	 * @return the mdrPort
	 */
	public int getMdrPort() {
		return mdrPort;
	}
	/**
	 * @param mdrPort the mdrPort to set
	 */
	public void setMdrPort(int mdrPort) {
		this.mdrPort = mdrPort;
	}

	/**
	 * @return the mcIP
	 */
	public InetAddress getMcIP() {
		return mcIP;
	}

	/**
	 * @param mcIP the mcIP to set
	 */
	public void setMcIP(InetAddress mcIP) {
		this.mcIP = mcIP;
	}

    /**
     * @return the peer Directory where his data will be saved
     */
    public String getPeerDir() {
        return peerDir;
    }
}