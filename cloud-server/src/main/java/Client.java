public class Client {
    private static final int PORT = 8137;
    private static Server server;

    public static void main(String[] args) {
        server = new Server(PORT, "files");
        server.start();
    }
}
