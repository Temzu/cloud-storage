package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class NioServer {

    private final ServerSocketChannel serverChannel = ServerSocketChannel.open();
    private final Selector selector = Selector.open();
    private final ByteBuffer buffer = ByteBuffer.allocate(5);
    private Path serverPath = Paths.get("files");

    public NioServer() throws IOException {
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (serverChannel.isOpen()) {
            selector.select(); // block
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NioServer();
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int read = 0;
        StringBuilder msg = new StringBuilder();
        while ((read = channel.read(buffer)) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                msg.append((char) buffer.get());
            }
            buffer.clear();
        }
        String command[] = msg.toString().replaceAll("[\n|\r]", "").split(" ");

        // нужно переделать на switch
        // и доделать обработки
        // возможно сделать отдельные методы для каждой команды
        if (command[0].equals(Commands.LS.getCommand())) {
            String files = Files.list(serverPath)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining(", "));
            files += "\n";
            channel.write(ByteBuffer.wrap(files.getBytes(StandardCharsets.UTF_8)));
        } else if (command[0].equals(Commands.CAT.getCommand())) {
            File file = new File(serverPath + "/" + command[1]);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    channel.write(ByteBuffer.wrap(((scanner.next() + "\n").getBytes(StandardCharsets.UTF_8))));
                }
            }
        } else if (command[0].equals(Commands.CD.getCommand())) {
            serverPath = Paths.get(String.valueOf(serverPath), command[1]);
            channel.write(ByteBuffer.wrap((serverPath.toString()+"\n").getBytes(StandardCharsets.UTF_8)));
        } else if (command[0].equals(Commands.MKDIR.getCommand())) {
            Files.createDirectory(Paths.get(String.valueOf(serverPath), command[1]));
            channel.write(ByteBuffer.wrap((String.valueOf(command[1] + " dir was created").getBytes())));
        } else if (command[0].equals(Commands.TOUCH.getCommand())) {
            Files.createFile(Paths.get(String.valueOf(serverPath), command[1]));
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }
}
