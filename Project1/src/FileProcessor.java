import java.io.File;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class FileProcessor{

    private Scanner terminal = new Scanner(System.in);

    public File loadFile(String filepath){
        File file = new File(filepath);
        if (!file.exists()){
            System.out.println("File " + filepath + " does not exist");
            return null;
        }
        if (!file.canRead()){
            System.out.println("Can not read file " + filepath);
            return null;
        }
        return file;
    }

    public File loadFileFromTerminal(){
        File file = null;

		//get filename and make sure it exists
		do {
			System.out.println("Filepath: ");
			String filepath = terminal.next();
			file = new File(filepath);
			file = loadFile(filepath);
        } while (file == null);
        return file;
    }
    public String getFileId(File file){
		String filename = file.getName();
		BasicFileAttributes attr = null;
		try {
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte[] hash = digest.digest((filename + attr.lastModifiedTime()).getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
	    for (byte b : hash) {
	        sb.append(String.format("%02X", b));
	    }
	    return sb.toString();
	}

	
}