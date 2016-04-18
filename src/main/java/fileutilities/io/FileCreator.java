package fileutilities.io;

import fileutilities.crypto.GCrypto;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author yannakisg
 */
public class FileCreator {

    private static final int BUFFER_SIZE = 16384 * 2;

    public static FileCreator build() {
        return new FileCreator();
    }

    private BufferedOutputStream out;

    private FileCreator() {
        out = null;
    }

    public void initStream(String fileName) throws FileNotFoundException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Illegar arguments");
        }

        out = new BufferedOutputStream(new FileOutputStream(fileName));
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        if (out == null) {
            throw new IllegalStateException("initStream should be called first");
        }
        if (buffer == null || buffer.length == 0 || length <= 0 || offset < 0) {
            throw new IllegalArgumentException("Illegar arguments");
        }

        out.write(buffer, 0, length);
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
        if (out == null) {
            throw new IllegalStateException("initStream should be called first");
        }
        File file = new File(fileName);

        BufferedInputStream bIn = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        while ((len = bIn.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        bIn.close();

        if (delete) {
            file.delete();
        }
    }

    public void writeFromFileAndSign(String fileName, boolean delete, GCrypto crypto) throws FileNotFoundException, IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Illegar arguments");
        }
        if (out == null) {
            throw new IllegalStateException("initStream should be called first");
        }
        File file = new File(fileName);

        BufferedInputStream bIn = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        while ((len = bIn.read(buffer)) > 0) {
            crypto.hmacBytes(buffer, len);
            out.write(buffer, 0, len);
        }
        bIn.close();

        if (delete) {
            file.delete();
        }
    }

    public void close() throws IOException {
        if (out == null) {
            throw new IllegalStateException("initStream should be called first");
        }

        out.close();
        out = null;
    }
}
