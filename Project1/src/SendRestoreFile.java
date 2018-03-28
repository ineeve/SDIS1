import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.io.File;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;

public class SendRestoreFile implements Runnable{

    private static final String CRLF = "\r\n";

    //bytes
	private final static int FILE_PORTION_SIZE = 64000;
   
    private MulticastSocket mcSocket;
    private File file;
    private String fileId;
    private Config config;
    private ChunksRequested chunksRequested;


    public SendRestoreFile(Config config, MulticastSocket mcSocket,File file, ChunksRequested chunksRequested ){
        FileProcessor fileProcessor = new FileProcessor();
        this.chunksRequested = chunksRequested;
        this.config = config;
        this.mcSocket = mcSocket;
        this.file = file;
        this.fileId = fileProcessor.getFileId(file);
    }

    private void sleepThread(int time){
		try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				System.out.println("Thread Interrupted");
				Thread.currentThread().interrupt();
			}
	}

	@Override
	public void run() {
        long fileSize = file.length();
        long totalChunks = Math.floorDiv(fileSize, FILE_PORTION_SIZE) + 1;
        for (int chunkNo = 0 ; chunkNo < totalChunks; chunkNo++){
            boolean packetSent = false;
            DatagramPacket getChunkPacket = makeGetChunkPacket(chunkNo);
            while(!packetSent){
				try {
					mcSocket.send(getChunkPacket);
					packetSent = true;
				}catch(IOException e){
					//buffer is full
					sleepThread(ThreadLocalRandom.current().nextInt(10, 400));
				}
            }
            chunksRequested.add(fileId, chunkNo);
            System.out.println("RESTORE: Sent GETCHUNK for " + fileId + "_" + chunkNo);
        }
	}

    private DatagramPacket makeGetChunkPacket(int chunkNo) {
		String getChunkMsgStr = "GETCHUNK 1.0 " + config.getPeerId() + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
		byte[] getChunkMsg = getChunkMsgStr.getBytes(Charset.forName("ISO_8859_1"));
		DatagramPacket packet = new DatagramPacket(getChunkMsg, getChunkMsg.length, config.getMcIP(), config.getMcPort());
		return packet;
	}

}