import utils.ThreadUtils;

import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.nio.charset.Charset;

public class SendRestoreFileEnh implements Runnable{

    private static final String CRLF = "\r\n";

    private int tcp_port;
    private MulticastSocket mcSocket;
    private File file;
    private String fileId;
    private Config config;
    private ChunksRequested chunksRequested;


    public SendRestoreFileEnh(Config config, MulticastSocket mcSocket,File file, ChunksRequested chunksRequested, int tcp_port){
        FileProcessor fileProcessor = new FileProcessor();
        this.chunksRequested = chunksRequested;
        this.config = config;
        this.mcSocket = mcSocket;
        this.file = file;
        this.fileId = fileProcessor.getFileId(file);
        this.tcp_port = tcp_port;
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
            chunksRequested.add(fileId, chunkNo);
        }
    }

    private DatagramPacket makeGetChunkPacket(int chunkNo) {
        String getChunkMsgStr = "GETCHUNK 2.0 " + config.getPeerId() + " " + fileId + " " + chunkNo + " " + CRLF;
        getChunkMsgStr += "localhost " + tcp_port + " " + CRLF + CRLF;
        byte[] getChunkMsg = getChunkMsgStr.getBytes(Charset.forName("ISO_8859_1"));
        DatagramPacket packet = new DatagramPacket(getChunkMsg, getChunkMsg.length, config.getMcIP(), config.getMcPort());
        return packet;
    }

}