import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port;
    private String rootFolder;

    public Server(int port, String rootFolder) {
        this.port = port;
        this.rootFolder = rootFolder;

    }

    public void start() {
        System.out.println(new File("files/sdfasdf.txt").exists());
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server was started on the port " + port);
            while (true){
                System.out.println("Waiting for client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected...");
                ClientHandler client = new ClientHandler(this, clientSocket);
                client.start();
            }
        }catch (IOException e){
            System.out.println("Server error");
            e.printStackTrace();
        }

    }

    public String getRootFolder() {
        return rootFolder;
    }
}
