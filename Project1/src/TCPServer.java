import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements Runnable{

    private ServerSocket serverSocket;
    private int portNumber;
    private ExecutorService pool = Executors.newCachedThreadPool();

    private ChunksRequested chunksRequested;
    private FilesRestored filesRestored;
    private Config config;

    public TCPServer(int portNumber, Config config, ChunksRequested chunksRequested, FilesRestored filesRestored){
        this.portNumber = portNumber;
        this.chunksRequested = chunksRequested;
        this.filesRestored = filesRestored;
        this.config = config;
    }


    private void createServerSocket(){
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveClientConnections(){
        while(true){
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                pool.execute(new AcceptClientConnection(config, clientSocket,chunksRequested,filesRestored));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void run() {
        createServerSocket();
        receiveClientConnections();
    }
}
