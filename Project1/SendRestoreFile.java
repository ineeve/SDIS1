import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class SendRestoreFile implements Runnable{

    private static final String CRLF = "\r\n";

   
    private MulticastSocket mcSocket;
    private String fileId;
    private Config config;
    private ChunksRequested chunksRequested;


    public SendRestoreFile(Config config, MulticastSocket mcSocket,String fileId, ChunksRequested chunksRequested ){
        this.config = config;
        this.mcSocket = mcSocket;
        this.fileId = fileId;
    }

	@Override
	public void run() {
		DatagramPacket getChunkPacket = makeGetChunkPacket(chunkNo);
	}

    private DatagramPacket makeGetChunkPacket(int chunkNo) {
		String getChunkMsgStr = "GETCHUNK 1.0 " + config.getPeerId() + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
		byte[] getChunkMsg = getChunkMsgStr.getBytes(Charset.forName("ISO_8859_1"));
		DatagramPacket packet = new DatagramPacket(getChunkMsg, getChunkMsg.length, config.getMcIP(), config.getMcPort());
		return packet;
	}

}