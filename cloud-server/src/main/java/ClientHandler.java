import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler extends Thread{
    private final Server server;
    private final Socket clientSocket;

    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Server networkServer, Socket clientSocket) {
        this.server = networkServer;
        this.clientSocket = clientSocket;
    }

    public void run(){
        doHandle(clientSocket);
    }

    private void doHandle(Socket socket) {

        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.execute(()->{
                try {
                    while (true){
                        String command = in.readUTF();
                        if (command.equals("/download")){
                            String fileName = in.readUTF();
                            String path = server.getRootFolder() + "/" + fileName.trim();
                            File sendFile = new File(path);

                            long fileSize = sendFile.length();
                            out.writeLong(fileSize);

                            //выгружаем файл в сокет
                            try(FileInputStream outFile = new FileInputStream(sendFile)){
                                transferFile(outFile, out, fileSize);
                            }catch (IOException e){
                                System.out.println("Download error");
                                e.printStackTrace();
                            }

                        }else if(command.equals("/upload")){

                            String fileName = in.readUTF();
                            String path = server.getRootFolder() + "/" + fileName;
                            long fileSize = in.readLong();
                            File loadFile = new File(path);
                            if (!loadFile.exists()) {
                                loadFile.createNewFile();
                            }
                            try (FileOutputStream inFile = new FileOutputStream(loadFile)){
                                transferFile(in, inFile, fileSize);
                            }catch (IOException e){
                                System.out.println("Upload error");
                                e.printStackTrace();
                            }

                        }
                    }
                }catch (IOException e){
                    System.out.println("Connection with client was closed");
                } finally {
                    closeConnection();
                }
            });

            executorService.shutdown();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void transferFile(InputStream in, OutputStream out, long fileSize) throws IOException {
        byte[] buff = new byte[8192];
        long byteCount = 0l;
        while (fileSize > byteCount) {
            int count = in.read(buff);
            if (count <= 0) break;
            byteCount = byteCount + count;
            out.write(buff, 0, count);
        }
    }

    private void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
