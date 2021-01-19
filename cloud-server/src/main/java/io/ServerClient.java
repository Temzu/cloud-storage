package io;

public class ServerClient {
    private static final int PORT = 8137;
    private static Server server;

    public static void main(String[] args) {
        server = new Server(PORT, "files");
        server.start();
    }
}
