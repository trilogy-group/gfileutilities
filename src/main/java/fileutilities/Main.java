package fileutilities;

import fileutilities.crypto.GFile;
import fileutilities.io.FileLister;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author yannakisg
 */
public class Main {

    private static void createOptionsMenu(Options options) {
        Option help = new Option("h", "print this message");
        help.setArgs(0);

        Option encrypt = new Option("e", "encrypt");
        encrypt.setArgs(0);

        Option decrypt = new Option("d", "decrypt");
        decrypt.setArgs(0);

        Option verify = new Option("v", "verify");
        verify.setArgs(0);
        
        Option vDecrypt = new Option("x", "verify and decrypt only if verification is true");
        vDecrypt.setArgs(0);
        
        Option file = new Option("f", "single filename or folder");
        file.setArgs(1);        

        Option files = new Option("m", "multiple filenames or folders");
        files.setArgs(Option.UNLIMITED_VALUES);
        
        Option output = new Option("o", "output folder or filename");
        output.setArgs(1);
        output.setRequired(false);
        
        Option password = new Option("p", "password");
        password.setArgs(1);
        password.setRequired(true);

        OptionGroup group1 = new OptionGroup();
        OptionGroup group2 = new OptionGroup();
        
        group1.addOption(help);
        group1.addOption(encrypt);
        group1.addOption(decrypt);        
        group1.addOption(verify);
        group1.addOption(vDecrypt);
        
        group2.addOption(files);
        group2.addOption(file);
        
        options.addOptionGroup(group1);
        options.addOptionGroup(group2);
        options.addOption(password);
        options.addOption(output);
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("gfileutilities", options);
        System.exit(0);
    }

    private static void parse(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help(options);
            }
            
            if (cmd.hasOption("e")) {
                Utilities.getInstance().setAction(Utilities.Action.ENCRYPT);
            } else if (cmd.hasOption("d")) {
                Utilities.getInstance().setAction(Utilities.Action.DECRYPT);
            } else if (cmd.hasOption("v")) {
                Utilities.getInstance().setAction(Utilities.Action.VERIFY);
            } else if (cmd.hasOption("x")) {
                Utilities.getInstance().setAction(Utilities.Action.VERIFY_DECRYPT);
            }
            
            if (cmd.hasOption("p")) {
                Utilities.getInstance().setPassword(cmd.getOptionValue("p"));
            }
            
            if (cmd.hasOption("o")) {
                Utilities.getInstance().setOutput(cmd.getOptionValue("o"));
            }
            
            if (cmd.hasOption("m")) {
                Utilities.getInstance().setFileAction(Utilities.FileAction.MULTIPLE);
                Utilities.getInstance().setFilePaths(cmd.getOptionValues("m"));
            } else if (cmd.hasOption("f")) {
                Utilities.getInstance().setFileAction(Utilities.FileAction.SINGLE);
                Utilities.getInstance().setFilePath(cmd.getOptionValue("f"));
            }            
            
        } catch (ParseException ex) {
            help(options);
        }
    }
    
    private static void performAction(GFile gfile, List<String> filePaths) throws IOException {        
        String password = Utilities.getInstance().getPassword();
        String output = Utilities.getInstance().getOutput();
        
        Utilities.Action action = Utilities.getInstance().getAction();
        
        if (action == Utilities.Action.DECRYPT) {
            for (String fileName : filePaths) {
                gfile.decrypt(password, fileName, output);
            }
        } else if (action == Utilities.Action.ENCRYPT) {
            gfile.encryptAndHmac(filePaths, password, output);
            System.out.println("File encrypted and is located at " + output);
        } else if (action == Utilities.Action.VERIFY) {
            for (String fileName : filePaths) {
                boolean ret = gfile.verifyHmac(password, fileName);
                System.out.println("Verification result for " + fileName + " is " + ret);
            }
        } else if (action == Utilities.Action.VERIFY_DECRYPT) {
            for (String fileName : filePaths) {
                boolean ret = gfile.verifyHmac(password, fileName);
                
                if (ret) {
                    gfile.decrypt(password, fileName, output);
                    System.out.println("Files decrypted and are located at " + output);
                } else {
                    System.out.println("Could not verify " + fileName);
                }
            }
        }
    }
    
    public static void main(String args[]) throws IOException {
        Options options = new Options();
        createOptionsMenu(options);

        parse(options, args);
        
        FileLister.getInstance().find(Utilities.getInstance().getFilePaths());        
        List<String> filePaths = FileLister.getInstance().getFiles();
        
        GFile gfile = new GFile();
        
        performAction(gfile, filePaths);
    }
}
