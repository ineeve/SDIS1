import utils.ThreadUtils;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.io.File;
import java.io.IOException;

public class SendRestoreFile implements Runnable{

   
    private MulticastSocket mcSocket;
    private File file;
    private String fileId;
    private ChunksRequested chunksRequested;


    public SendRestoreFile(MulticastSocket mcSocket,File file, ChunksRequested chunksRequested) {
        this.chunksRequested = chunksRequested;
        this.mcSocket = mcSocket;
        this.file = file;
        this.fileId = FileProcessor.getFileId(file);
    }


	@Override
	public void run() {
        long fileSize = file.length();
        long totalChunks = Math.floorDiv(fileSize, Config.MAX_CHUNK_SIZE) + 1;
        for (int chunkNo = 0 ; chunkNo < totalChunks; chunkNo++){
            boolean packetSent = false;
            DatagramPacket getChunkPacket = makeGetChunkPacket(chunkNo);
            while(!packetSent){
				try {
					mcSocket.send(getChunkPacket);
					packetSent = true;
				}catch(IOException e){
					//buffer is full
                    ThreadUtils.waitBetween(10,400);
				}
            }
            System.out.println("Chunk " + chunkNo + " requested to restore");
            chunksRequested.add(fileId, chunkNo);
        }
	}

    private DatagramPacket makeGetChunkPacket(int chunkNo) {
		byte[] getChunkMsg = Messages.getGETCHUNKHeader(fileId, chunkNo);
		DatagramPacket packet = new DatagramPacket(getChunkMsg, getChunkMsg.length, Config.getMcIP(), Config.getMcPort());
		return packet;
	}

}