package fileutilities.thread;

import fileutilities.ByteUtils;
import fileutilities.io.NioFileCreator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yannakisg
 */
public class Worker implements Runnable {

    public enum Status {

        FIRST_ENCRYPT_BLOCK,
        FIRST_FILE_BLOCK,
        INSIDE_BLOCK
    }

    public static Worker build(String folderName) {
        return new Worker(folderName);
    }

    private BlockingDeque<Data> deque;
    private String folderName;

    private Worker(String folderName) {
        this.deque = new LinkedBlockingDeque<>();
        this.folderName = folderName;
    }

    public void start() {
        new Thread(this).start();
    }

    public void put(byte[] data, Status status) {
        deque.addLast(new Data(data, status));
    }

    public void stop() {
        deque.addLast(new Data());
    }

    @Override
    public void run() {

        NioFileCreator creator = NioFileCreator.build();
        long totalFiles = 0;
        int fileNameLen;
        String fileName;
        long totalSize = 0;

        while (true) {
            try {
                Data data = deque.take();

                if (data.isEmpty()) {
                    break;
                }
                
                if (data.status == Status.FIRST_ENCRYPT_BLOCK) {
                    totalFiles = ByteUtils.getInstance().bytesToLong(data.data);
                    fileNameLen = ByteUtils.getInstance().bytesToInt(data.data, Long.BYTES);
                    fileName = new String(data.data, Long.BYTES + Integer.BYTES, fileNameLen, "UTF8");
                    creator.initStream(folderName + fileName);

                    totalSize = ByteUtils.getInstance().bytesToLong(data.data, Long.BYTES + Integer.BYTES + fileNameLen);

                    if (totalSize > (data.data.length - (Long.BYTES + Integer.BYTES + fileNameLen + Long.BYTES))) {
                        creator.write(data.data, Long.BYTES + Integer.BYTES + fileNameLen + Long.BYTES, data.data.length);
                        totalSize -= data.data.length - (Long.BYTES + Integer.BYTES + fileNameLen + Long.BYTES);
                    } else {
                        creator.write(data.data, Long.BYTES + Integer.BYTES + fileNameLen + Long.BYTES, (int) (totalSize));
                        creator.close();
                        totalFiles--;

                        if (totalFiles != 0) {
                            deque.addFirst(new Data(Arrays.copyOfRange(data.data, (int) totalSize, data.data.length), Status.FIRST_FILE_BLOCK));
                        }
                    }
                } else if (data.status == Status.FIRST_FILE_BLOCK) {
                    fileNameLen = ByteUtils.getInstance().bytesToInt(data.data);
                    fileName = new String(data.data, Integer.BYTES, fileNameLen, "UTF8");
                    
                    creator.initStream(folderName + fileName);

                    totalSize = ByteUtils.getInstance().bytesToLong(data.data, Integer.BYTES + fileNameLen);

                    if (totalSize > (data.data.length - (Long.BYTES + Integer.BYTES + fileNameLen))) {
                        creator.write(data.data, Long.BYTES + Integer.BYTES + fileNameLen, data.data.length);
                        totalSize -= data.data.length - (Integer.BYTES + fileNameLen + Long.BYTES);
                    } else {
                        creator.write(data.data, Integer.BYTES + fileNameLen + Long.BYTES, (int) (totalSize));
                        creator.close();

                        totalFiles--;

                        if (totalFiles != 0) {
                            deque.addFirst(new Data(Arrays.copyOfRange(data.data, (int) (totalSize), data.data.length), Status.FIRST_FILE_BLOCK));
                        }
                    }
                } else {
                    if (totalSize > data.data.length) {
                        creator.write(data.data, 0, data.data.length);
                        totalSize -= data.data.length;
                    } else {
                        creator.write(data.data, 0, (int) totalSize);
                        creator.close();

                        totalFiles--;
                        if (totalFiles != 0) {
                            deque.addFirst(new Data(Arrays.copyOfRange(data.data, (int) (totalSize), data.data.length), Status.FIRST_FILE_BLOCK));
                        }
                    }
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class Data {

        byte[] data;
        Status status;

        protected Data(byte[] data, Status status) {
            this.data = data;
            this.status = status;
        }

        protected Data() {
            this(null, Status.INSIDE_BLOCK);
        }

        protected boolean isEmpty() {
            return data == null;
        }
    }

}
