import java.io.*;
import java.net.Socket;

public class Client {

    private static Socket socket;
    private static final int PORT = 8137;
    private static final String HOST = "127.0.0.1";
    private static String rootFolder = "D:/tests/download_test";

    public static void main(String[] args) {
        DataInputStream is = null;
        DataOutputStream out = null;

        try {
            socket = new Socket(HOST, PORT);
            is = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            String fileName = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String command;
                while (true) {
                    System.out.println("Enter the command:");
                    System.out.println("/download");
                    System.out.println("/upload");
                    command = reader.readLine();
                    if (command.equals("/download")) {
                        out.writeUTF(command);
                        System.out.println("Enter the file name:");

                        fileName = reader.readLine();
                        out.writeUTF(fileName);

                        long fileSize = is.readLong();
                        System.out.println(fileSize);

                        File loadFile = new File(rootFolder + "/" + fileName);
                        if (!loadFile.exists()) {
                            loadFile.createNewFile();
                        }

                        try (FileOutputStream inFile = new FileOutputStream(loadFile)){
                            transferFile(is, inFile, fileSize);
                            inFile.flush();
                        }catch (IOException e){
                            System.out.println("download error");
                            e.printStackTrace();
                        }
                        System.out.println("download successful");
                    } else if (command.equals("/upload")) {
                        out.writeUTF(command);
                        System.out.println("Enter the full file name:");

                        fileName = reader.readLine();
                        File sendFile = new File(fileName);
                        out.writeUTF(sendFile.getName());

                        long fileSize = sendFile.length();
                        out.writeLong(fileSize);

                        try(FileInputStream outFile = new FileInputStream(sendFile)){
                            transferFile(outFile, out, fileSize);
                            out.flush();
                        }catch (IOException e){
                            System.out.println("Upload error");
                            e.printStackTrace();
                        }
                        System.out.println("Upload successful");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed connection to: " + HOST + ":" + PORT);
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void transferFile(InputStream in, OutputStream out, long fileSize) throws IOException {
        byte[] buff = new byte[8192];
        long byteCount = 0l;
        while (fileSize > byteCount) {
            int count = in.read(buff);
            if (count <= 0) break;
            byteCount = byteCount + count;
            out.write(buff, 0, count);
        }
    }
}

