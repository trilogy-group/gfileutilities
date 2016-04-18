package fileutilities.io;

import fileutilities.ByteUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author yannakisg
 */
public class FileReader {

    public static FileReader build() {
        return new FileReader();
    }

    private BufferedInputStream bIn;

    private FileReader() {
        bIn = null;
    }

    public void initStream(File file) throws FileNotFoundException {
        bIn = new BufferedInputStream(new FileInputStream(file));
    }

    public void initStream(String fileName) throws FileNotFoundException {
        initStream(new File(fileName));
    }

    public long readLong() throws IOException {
        if (bIn == null) {
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
        if (bIn == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        if (buffer == null || buffer.length <= 0) {
            throw new IllegalArgumentException("Invalid input arguments");
        }

        if (remData < buffer.length) {
            return bIn.read(buffer, 0, (int) remData);
        } else {
            return bIn.read(buffer);
        }
    }

    public int readInt() throws IOException {
        if (bIn == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        byte[] data = readBytes(Integer.BYTES);

        return ByteUtils.getInstance().bytesToInt(data);
    }

    public byte[] readBytes(int size) throws IOException {
        if (bIn == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        byte[] data = new byte[size];

        int len = 0;
        while ((len += bIn.read(data, len, size - len)) != size);

        return data;
    }

    public int readBytes(byte[] buffer) throws IOException {
        if (bIn == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("Invalid input argument");
        }

        return bIn.read(buffer);
    }

    public void close() throws IOException {
        if (bIn != null) {
            bIn.close();
            bIn = null;
        }
    }
}
