package fileutilities.io;

import fileutilities.crypto.GCrypto;
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
public class NioFileCreator {

    private static final int BUFFER_SIZE = 16384 * 2;

    public static NioFileCreator build() {
        return new NioFileCreator();
    }

    private RandomAccessFile file;
    private FileChannel channel;

    private NioFileCreator() {
        file = null;
        channel = null;
    }

    public void initStream(String fileName) throws FileNotFoundException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Illegar arguments");
        }
        
        File temp = new File(fileName);
        if (temp.exists()) {
            temp.delete();
        }
        
        file = new RandomAccessFile(fileName, "rw");
        channel = file.getChannel();
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }
        if (buffer == null || buffer.length == 0 || length < 0 || offset < 0) {
            throw new IllegalArgumentException("Illegar arguments");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.position(offset);
        byteBuffer.limit(length);

        channel.write(byteBuffer);
    }

    public void write(byte[] buffer, int length) throws IOException {
        if (buffer != null) {
            write(buffer, 0, length);
        }
    }

    public void write(byte[] buffer) throws IOException {
        if (buffer != null) {
            write(buffer, 0, buffer.length);
        }
    }

    public void writeFromFile(String fileName, boolean delete) throws FileNotFoundException, IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Illegar arguments");
        }
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        File tempFile = new File(fileName);
        NioFileReader reader = NioFileReader.build();
        reader.initStream(tempFile);

        if (delete) {
            tempFile.delete();
        }

        channel.transferFrom(reader.getChannel(), 0, reader.getChannel().size());
        reader.close();
    }

    public void writeFromFileAndSign(String fileName, boolean delete, GCrypto crypto) throws FileNotFoundException, IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Illegar arguments");
        }
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }
        File tempFile = new File(fileName);

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        NioFileReader reader = NioFileReader.build();
        reader.initStream(tempFile);

        int len;
        while ((len = reader.read(buffer)) > 0) {
            crypto.hmacBytes(buffer.array(), len);
            buffer.limit(len);
            buffer.position(0);
            int l = channel.write(buffer);
            buffer.position(0);
            buffer.limit(BUFFER_SIZE);
        }

        reader.close();

        if (delete) {
            tempFile.delete();
        }
    }

    public void close() throws IOException {
        if (channel == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        channel.close();
        file.close();
        channel = null;
        file = null;
    }

}
