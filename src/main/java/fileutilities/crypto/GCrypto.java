package fileutilities.crypto;

import fileutilities.ByteUtils;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author yannakisg
 */
public class GCrypto {

    private static final String SECRET_KEY_FACTORY = "PBKDF2WithHmacSHA256";
    private static final String HMAC_ALGORITHM = "HmacSHA256"; 
    protected static final int HMAC_SIZE = 32;
    protected static final int SALT_SIZE = 8;
    protected static final int IV_SIZE = 16;
    protected static final int AES_BLOCK_SIZE = 16;
    private static final int KEY_LEN = 256;
    private static final int ITER_COUNTS = 65536;
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private Cipher cipher;
    private SecretKey key;
    private String password;
    private byte[] iv;
    private byte[] salt;
    private Mac hmac;
    private CipherOutputStream cOut;

    public static GCrypto build() {
        return new GCrypto();
    }

    private GCrypto() {
        iv = null;
        salt = null;
        key = null;
        hmac = null;
        cipher = null;
    }

    protected void deriveEncryptKeys(String password) {
        try {
            this.password = password;
            SecureRandom random = new SecureRandom();
            salt = new byte[SALT_SIZE];
            random.nextBytes(salt);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER_COUNTS, KEY_LEN);

            key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), SECRET_KEY_ALGORITHM);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
        }

    }

    protected void deriveDecryptKeys(String password) {
        if (salt == null) {
            throw new IllegalStateException("Unknown salt");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        
        this.password = password;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER_COUNTS, KEY_LEN);
            key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), SECRET_KEY_ALGORITHM);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
        }
    }
    
    protected void initHmacProcedure(String password) {
        try {
            this.password = password;
            hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec hmac_key = new SecretKeySpec(this.password.getBytes("UTF8"), HMAC_ALGORITHM);
            hmac.init(hmac_key);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException ex) {
            Logger.getLogger(GCrypto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void initHmacProcedure() {
        try {
            hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec hmac_key = new SecretKeySpec(password.getBytes("UTF8"), HMAC_ALGORITHM);
            hmac.init(hmac_key);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException ex) {
            Logger.getLogger(GCrypto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void initCipherDecryption() {
        try {
            if (key == null) {
                throw new IllegalStateException("deriveDecryptKeys should be called first");
            }
            if (iv == null) {
                throw new IllegalStateException("Unknown iv");
            }

            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
        }
    }

    protected void initCipherEncryption() {
        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidParameterSpecException ex) {
        }
    }

    protected void initOutputStream(String fileName) throws FileNotFoundException {
        if (cipher == null) {
            throw new IllegalStateException("initCipherEncryption or initCipherDecryption was never called.");
        }

        cOut = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)), cipher);
    }
    
    protected void hmacSalt() {
        if (hmac == null) {
            throw new IllegalStateException("initHmacProcedure was never called.");
        }
        
        if (salt == null) {
         throw new IllegalStateException("Unknown salt");
        }
        
        hmac.update(salt);
     } 
    
    protected void hmacIV() {
        if (hmac == null) {
            throw new IllegalStateException("initHmacProcedure was never called.");
        }
        
        if (iv == null) {
         throw new IllegalStateException("Unknown iv");
        }
        
        hmac.update(iv);
     } 
    
    public void hmacBytes(byte[] data, int len) {
        if (hmac == null) {
            throw new IllegalStateException("initHmacProcedure was never called.");
        }
        
        if (data == null || data.length < len || len <= 0) {
            throw new IllegalArgumentException("Invalid input arguments");
        } 
        
        hmac.update(data, 0, len);
    }
    
    protected byte[] finalizeHmacAndGet() {
        if (hmac == null) {
            throw new IllegalStateException("initHmacProcedure was never called.");
        }
        
        return hmac.doFinal();
    }

    protected void setSalt(byte[] salt) {
        this.salt = salt;
    }

    protected void setIV(byte[] iv) {
        this.iv = iv;
    }

    protected byte[] getSalt() {
        return this.salt;
    }

    protected byte[] getIV() {
        return this.iv;
    }

    protected byte[] readBuffer(byte[] buffer, int length) {
        return readBuffer(buffer, 0, length);
    }

    protected byte[] readDoFinal() {
        if (cipher == null) {
            throw new IllegalStateException("initCipherEncryption should be called first");
        }

        try {
            return cipher.doFinal();
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(GCrypto.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    protected byte[] readBuffer(byte[] buffer, int offset, int length) {
        if (buffer == null || buffer.length == 0 || offset < 0 || length < 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        if (cipher == null) {
            throw new IllegalStateException("initCipherEncryption should be called first");
        }

        return cipher.update(buffer, 0, length);
    }

    protected int writeTo(byte[] buffer, int offset, String string) {
        byte[] bytes;
        try {
            bytes = string.getBytes("UTF8");
            int len = bytes.length;

            return writeTo(buffer, offset, bytes, len);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GCrypto.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    protected int writeTo(byte[] buffer, int offset, int number) {
        byte[] bytes = ByteUtils.getInstance().intToBytes(number);
        int len = bytes.length;

        return writeTo(buffer, offset, bytes, len);
    }

    protected int writeTo(byte[] buffer, int offset, long number) {
        byte[] bytes = ByteUtils.getInstance().longToBytes(number);
        int len = bytes.length;
        System.out.println("\t" + len);
        return writeTo(buffer, offset, bytes, len);
    }
    
    protected byte[] writeDoFinal() {
        try {
            return cipher.doFinal();
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(GCrypto.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    protected byte[] writeTo(ByteBuffer buffer) {
        byte[] cData = cipher.update(buffer.array(), 0, buffer.position());
        return cData;
    }

    protected int writeTo(byte[] buffer, int offset, byte[] data, int length) {
        if (buffer == null || buffer.length == 0 || offset < 0 || length < 0 || data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        if (cipher == null) {
            throw new IllegalStateException("initCipherEncryption should be called first");
        }

        byte[] cData = cipher.update(data, 0, length);
        if (cData != null && cData.length > 0) {
            System.out.println(offset + " " + cData.length + " | " + buffer.length + " " + length);
            System.arraycopy(cData, 0, buffer, offset, cData.length);

            System.out.println("Writing [" + offset + "] -> " + cData.length);
            return cData.length;
        } else {
            return 0;
        }
    }

    protected void finalizeCipher(byte[] buffer, int offset) {
        if (cipher == null) {
            throw new IllegalStateException("initCipherEncryption should be called first");
        }
        try {
            byte[] data = cipher.doFinal();
            if (data != null && data.length > 0) {
                System.arraycopy(data, 0, buffer, offset, data.length);
            }
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(GCrypto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void write(int number) throws IOException {
        write(ByteUtils.getInstance().intToBytes(number));
    }

    protected void write(long number) throws IOException {
        write(ByteUtils.getInstance().longToBytes(number));
    }

    protected void write(String string) throws IOException {
        if (string == null || string.equals("")) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        write(string.getBytes("UTF8"));
    }

    protected void write(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        write(bytes, 0, bytes.length);
    }

    protected void write(byte[] bytes, int length) throws IOException {
        if (bytes == null || bytes.length == 0 || length <= 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        write(bytes, 0, length);
    }

    protected void write(byte[] bytes, int offset, int length) throws IOException {
        if (cOut == null) {
            throw new IllegalStateException("initOutputStream was never called.");
        }

        cOut.write(bytes, offset, length);
    }

    protected void closeStream() throws IOException {
        if (cOut == null) {
            throw new IllegalStateException("initOutputStream was never called.");
        }
        cOut.close();
        cOut = null;
    }
}
