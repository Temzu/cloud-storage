package nio;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class ChannelsUtils {

    public static void main(String[] args) throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile("out.txt", "rw");
        FileChannel fileChannel = raf.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) 65);
        buffer.put((byte) 66);
        buffer.put((byte) 67);
        buffer.flip();
        System.out.println(Arrays.toString(buffer.array()));
        while (buffer.hasRemaining()) {
            System.out.println((char) buffer.get());
        }
        buffer.rewind();
        buffer.clear();
        buffer.put((byte) 68);
        buffer.put((byte) 68);
        buffer.put((byte) 68);
        buffer.put((byte) 68);
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println((char) buffer.get());
        }
    }
}
