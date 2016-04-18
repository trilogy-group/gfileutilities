package fileutilities.crypto;

import fileutilities.ByteUtils;
import fileutilities.io.NioFileCreator;
import fileutilities.io.NioFileReader;
import fileutilities.thread.Worker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yannakisg
 */
public class GFile {

   
    // Encrypt Block: |Number of Files|Length of FileName|FileName|Length of Data|Data| 
    // Output File  : |Salt|IV|Encrypt|HMAC-256|
    private static final int BLOCKSIZE = 16384 * 2;
    private static final String EXTENSION = ".gfile";

    private int totalSize;
    private long numberOfFiles;
    private List<File> filePaths;
    private final GCrypto crypto;
    private int paddingBytes;
    
    public GFile() {
        this.totalSize = 0;
        this.numberOfFiles = 0;
        this.crypto = GCrypto.build();
    }
    
    private void createFilePath(List<String> list) {
        this.filePaths = new ArrayList<>();
        for (String s : list) {
            try {
                File f = new File(s);
                long fileLength = f.length();
                byte[] sBytes = s.getBytes("UTF8");

                if (f.exists() && f.isFile()) {
                    totalSize += Integer.BYTES + Long.BYTES;
                    totalSize += sBytes.length + fileLength;
                    filePaths.add(f);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(GFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        totalSize += Long.BYTES;

        paddingBytes = totalSize % GCrypto.AES_BLOCK_SIZE;
        totalSize += paddingBytes;

        totalSize += GCrypto.IV_SIZE + GCrypto.SALT_SIZE;
    }

    public void decrypt(String password, String inputFile, String outputFolder) throws FileNotFoundException, IOException {
        if (password == null || password.equals("") || inputFile == null || inputFile.equals("") || outputFolder == null || outputFolder.equals("")) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        File inFile = new File(inputFile);
        if (!inFile.exists()) {
            throw new IllegalArgumentException("Input file does not exist.");
        }

        if (!outputFolder.endsWith(File.separator)) {
            outputFolder += File.separator;
        }

        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Worker worker = Worker.build(outputFolder);
        worker.start();

        NioFileReader reader = NioFileReader.build();
        reader.initStream(inFile);

        long total = inFile.length() - GCrypto.HMAC_SIZE - GCrypto.SALT_SIZE - GCrypto.IV_SIZE;
        byte[] salt = reader.readBytes(GCrypto.SALT_SIZE);
        byte[] iv = reader.readBytes(GCrypto.IV_SIZE);

        byte[] dData;
        int len;
        byte[] buffer = new byte[BLOCKSIZE];

        crypto.setSalt(salt);
        crypto.setIV(iv);

        crypto.deriveDecryptKeys(password);
        crypto.initCipherDecryption();

        boolean isFirst = true;
        while (total != 0 && (len = reader.readAtMostBytes(buffer, total)) > 0) {
            total -= len;
            dData = crypto.readBuffer(buffer, len);
            if (dData != null) {
                if (isFirst) {
                    worker.put(dData, Worker.Status.FIRST_ENCRYPT_BLOCK);
                } else {
                    worker.put(dData, Worker.Status.INSIDE_BLOCK);
                }
                isFirst = false;
            }
        }

        dData = crypto.readDoFinal();
        if (dData != null && dData.length > 0) {
            if (isFirst) {
                worker.put(dData, Worker.Status.FIRST_ENCRYPT_BLOCK);
            } else {
                worker.put(dData, Worker.Status.INSIDE_BLOCK);
            }
        }

        worker.stop();
    }

    public void encryptAndHmac(List<String> paths, String password, String outputFileName) throws IOException {
        if (paths == null || paths.isEmpty() || password == null || password.equals("") || outputFileName == null || outputFileName.equals("")) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        
        createFilePath(paths);
        this.numberOfFiles = filePaths.size();

        crypto.deriveEncryptKeys(password);
        crypto.initCipherEncryption();

        NioFileReader reader = NioFileReader.build();

        NioFileCreator fileCreator = NioFileCreator.build();
        fileCreator.initStream(outputFileName + EXTENSION);

        fileCreator.write(crypto.getSalt());
        fileCreator.write(crypto.getIV());

        crypto.initHmacProcedure();
        crypto.hmacSalt();
        crypto.hmacIV();

        ByteBuffer byteBuffer = ByteBuffer.allocate(BLOCKSIZE);
        byteBuffer.put(ByteUtils.getInstance().longToBytes(numberOfFiles));
        byte[] data;

        for (int i = 0; i < filePaths.size(); i++) {
            byteBuffer.put(ByteUtils.getInstance().intToBytes(filePaths.get(i).getName().length()));
            byteBuffer.put(filePaths.get(i).getName().getBytes("UTF8"));
            if (i == filePaths.size() - 1) {
                byteBuffer.put(ByteUtils.getInstance().longToBytes(filePaths.get(i).length() + paddingBytes));
            } else {
                byteBuffer.put(ByteUtils.getInstance().longToBytes(filePaths.get(i).length()));
            }

            reader.initStream(filePaths.get(i));

            while (reader.read(byteBuffer) > 0) {
                data = crypto.writeTo(byteBuffer);
                if (data != null && data.length > 0) {
                    crypto.hmacBytes(data, data.length);
                    fileCreator.write(data);
                }

                byteBuffer.position(0);
            }

            reader.close();
        }

        data = crypto.writeDoFinal();
        if (data != null && data.length > 0) {
            crypto.hmacBytes(data, data.length);
            fileCreator.write(data);
        }

        fileCreator.write(crypto.finalizeHmacAndGet());
        fileCreator.close();
    }

    public boolean verifyHmac(String password, String filePath) throws FileNotFoundException, IOException {
        crypto.initHmacProcedure(password);

        NioFileReader reader = NioFileReader.build();
        File inFile = new File(filePath);
        long total = inFile.length() - GCrypto.HMAC_SIZE;

        reader.initStream(inFile);

        int len;
        byte[] buffer = new byte[BLOCKSIZE];
        while (total != 0 && (len = reader.readAtMostBytes(buffer, total)) > 0) {
            total -= len;
            crypto.hmacBytes(buffer, len);
        }

        byte[] hmacInFile = reader.readBytes(GCrypto.HMAC_SIZE);
        byte[] hmacComp = crypto.finalizeHmacAndGet();

        return Arrays.equals(hmacInFile, hmacComp);
    }
}
