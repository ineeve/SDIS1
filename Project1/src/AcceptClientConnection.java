import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;

public class AcceptClientConnection implements Runnable {

    private static final String CRLF = "\r\n";

    private ChunksRequested chunksRequested;
    private FilesRestored filesRestored;

    private Socket clientSocket;
    private BufferedReader in;
    private StringBuilder messageReceived;

    public AcceptClientConnection(Socket clientSocket, ChunksRequested chunksRequested, FilesRestored filesRestored){
        this.clientSocket = clientSocket;
        this.chunksRequested = chunksRequested;
        this.filesRestored = filesRestored;
        messageReceived = new StringBuilder();
    }

    private String readLine(){
        String inputLine = null;
        try {
            inputLine = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return inputLine;
    }

    private void appendInput(String inputLine){
        System.out.println("AcceptClientConnection " + inputLine);
        messageReceived.append(inputLine);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String inputLine;


        while ( (inputLine = readLine()) != null){
            appendInput(inputLine);
        }
        parseReceivedChunk();

    }

    private boolean parseReceivedChunk(){
        String msg = messageReceived.toString().trim();
        String crlf = new String(CRLF);
        String[] splitMessage = msg.split(crlf + crlf);
        String head[] = splitMessage[0].split("\\s+");
        String fileId = head[3];
        int chunkNo = Integer.parseInt(head[4]);
        if (!chunksRequested.wasRequested(fileId,chunkNo)) return false;
        if (filesRestored.containsChunk(fileId, chunkNo)) return false;
        byte[] body = splitMessage[1].getBytes(Charset.forName("ISO_8859_1"));
        filesRestored.addChunk(fileId, chunkNo, body);
        if (body.length < Config.MAX_CHUNK_SIZE){
            filesRestored.setReceivedLastChunk(fileId);
        }
        return true;
    }
}
