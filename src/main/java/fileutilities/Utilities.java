package fileutilities;

/**
 *
 * @author yannakisg
 */
public class Utilities {
    private static final Utilities _instance = new Utilities();
    
    public static Utilities getInstance() {
        return _instance;
    }
    
    public enum Action {
        ENCRYPT,
        DECRYPT,
        VERIFY,
        VERIFY_DECRYPT
    }
    
    public enum FileAction {
        MULTIPLE,
        SINGLE
    }
    
    private Action action;
    private FileAction fileAction;
    private String password;
    private String[] filePaths;
    private String output;
    
    private Utilities () {
        this.action = Action.DECRYPT;
        this.fileAction = FileAction.SINGLE;
        this.password = "foo";
        this.filePaths = new String[1];
        this.output = "output";
    }
    
    public Action getAction() {
        return this.action;
    }
    
    public void setAction(Action action) {
        this.action = action;
    }
    
    public String getOutput() {
        return this.output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setFilePath(String filePath) {
        this.filePaths[0] = filePath;
    }
    
    public void setFilePaths(String[] filePaths) {
        this.filePaths = new String[filePaths.length];
        
        System.arraycopy(filePaths, 0, this.filePaths, 0, filePaths.length);
    }
    
    public String[] getFilePaths() {
        return this.filePaths;
    }
    
    public FileAction getFileAction() {
        return this.fileAction;
    }
    
    public void setFileAction(FileAction fileAction) {
        this.fileAction = fileAction;
    }
}
