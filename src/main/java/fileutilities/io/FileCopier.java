package fileutilities.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 *
 * @author yannakisg
 */
public class FileCopier {
    private static FileCopier _instance = null;
    
    public static FileCopier getInstance() {
        if (_instance == null) {
            _instance = new FileCopier();
        }
        
        return _instance;
    }
    
    private FileCopier() {
        
    }
    
    public void copyFile(String fileName, String newFileName) throws IOException {
        File input = new File(fileName);
        File output = new File(newFileName);
        Files.copy(input.toPath(), output.toPath());
    }
}
