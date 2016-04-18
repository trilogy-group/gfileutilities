package fileutilities;

/**
 *
 * @author yannakisg
 */
public class ByteUtils {

    private static final ByteUtils _instance = new ByteUtils();

    public static ByteUtils getInstance() {
        return _instance;
    }

    private ByteUtils() {

    }

    public byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];

        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= Long.BYTES;
        }

        return result;
    }

    public long bytesToLong(byte[] b, int offset) {
        long result = 0;

        for (int i = offset; i < offset + Long.BYTES; i++) {
            result <<= Long.BYTES;
            result |= (b[i] & 0xFF);
        }

        return result;
    }

    public long bytesToLong(byte[] b) {
        return bytesToLong(b, 0);
    }

    public byte[] intToBytes(int in) {
        byte[] result = new byte[Integer.BYTES];

        for (int i = Integer.BYTES - 1; i >= 0; i--) {
            result[i] = (byte) (in & 0xFF);
            in >>= Integer.BYTES;
        }

        return result;
    }

    public int bytesToInt(byte[] b, int offset) {
        int result = 0;

        for (int i = offset; i < offset + Integer.BYTES; i++) {
            result <<= Integer.BYTES;
            result |= (b[i] & 0xFF);
        }

        return result;
    }

    public int bytesToInt(byte[] b) {
        return bytesToInt(b, 0);
    }
}
