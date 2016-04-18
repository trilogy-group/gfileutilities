package fileutilities.io;

import fileutilities.Utilities;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author yannakisg
 */
public class FileLister {

    private static final FileLister _instance = new FileLister();

    public static FileLister getInstance() {
        return _instance;
    }

    private final List<String> files;

    private FileLister() {
        this.files = new ArrayList<>();
    }
    
    public void find(String[] filePaths) {
        for (String fileName : filePaths) {
            File file = new File(fileName);
            if (file.exists()) {
                 if (file.isDirectory()) {
                     find(fileName, new FilenameFilter() {

                         @Override
                         public boolean accept(File dir, String name) {
                             return (!(name.endsWith(".") || name.endsWith("..")));
                         }
                     });
                 } else if (file.isFile()) {
                     files.add(fileName);
                 }            
            }
        }
    }

    public void find(String folderName, FilenameFilter filter) {
        File folder = new File(folderName);

        if ((!folder.exists())) {
            return;
        }

        if (!folder.isDirectory()) {
            files.add(folderName);
        } else {
            Stack<File> stack = new Stack<>();
            stack.push(folder);

            while (!stack.isEmpty()) {
                File file = stack.pop();
                if (file.isDirectory()) {
                    File[] localFiles = file.listFiles(filter);
                    for (File f : localFiles) {
                        stack.push(f);
                    }
                } else {
                    files.add(file.getAbsolutePath());
                }
            }
        }
    }

    public List<String> getFiles() {
        return this.files;
    }
}
