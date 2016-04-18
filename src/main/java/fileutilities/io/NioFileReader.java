package fileutilities.io;

import fileutilities.ByteUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author yannakisg
 */
public class NioFileReader {

    public static NioFileReader build() {
        return new NioFileReader();
    }

    private RandomAccessFile file;
    private FileChannel channel;

    private NioFileReader() {
        file = null;
        channel = null;
    }

    public void initStream(String fileName) throws FileNotFoundException {
        initStream(new File(fileName));
    }

    public void initStream(File inFile) throws FileNotFoundException {
        file = new RandomAccessFile(inFile, "r");
        channel = file.getChannel();
    }

    public long readLong() throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        byte[] data = readBytes(Long.BYTES);

        return ByteUtils.getInstance().bytesToLong(data);
    }

    public String readString(int size) throws IOException {
        byte[] data = readBytes(size);
        String str = new String(data, "UTF8");
        return str;
    }

    public int readAtMostBytes(byte[] buffer, long remData) throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        if (buffer == null || buffer.length <= 0) {
            throw new IllegalArgumentException("Invalid input arguments");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        int len;

        if (remData < buffer.length) {
            byteBuffer.limit((int) remData);
        }
        len = channel.read(byteBuffer);

        return len;
    }

    public int readInt() throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        byte[] data = readBytes(Integer.BYTES);

        return ByteUtils.getInstance().bytesToInt(data);
    }

    public byte[] readBytes(int size) throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        byte[] data = new byte[size];

        ByteBuffer buffer = ByteBuffer.allocate(size);

        channel.read(buffer);

        byte[] b = buffer.array();
        for (int i = 0; i < size; i++) {
            data[i] = b[i];
        }

        return data;
    }

    public int readBytes(byte[] buffer) throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("Invalid input argument");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        return channel.read(byteBuffer);
    }

    public void close() throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        channel.close();

        channel = null;
        file = null;
    }

    public int read(ByteBuffer buffer) throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        if (buffer == null) {
            throw new IllegalArgumentException("Invalid input argument");
        }

        return channel.read(buffer);
    }

    protected FileChannel getChannel() {
        return this.channel;
    }
}
