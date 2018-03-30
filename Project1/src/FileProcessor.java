import java.io.File;
import java.io.FilenameFilter;
import java.util.Scanner;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
			System.out.print("\nFilepath: ");
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

    public static void deleteFile(String pathStr) {
    	Path path = Paths.get(pathStr);
    	try {
    	    Files.delete(path);
    	} catch (NoSuchFileException x) {
    	    System.err.format("%s: no such file or directory%n", path);
    	} catch (DirectoryNotEmptyException x) {
    	    System.err.format("%s not empty%n", path);
    	} catch (IOException x) {
    	    // File permission problems are caught here.
    	    System.err.println(x);
    	}
    }

	public static void deleteFilesStartingWith(String prefix, String folderPath) {
		final File folder = new File(folderPath);
		final File[] files = folder.listFiles( new FilenameFilter() {
		    @Override
		    public boolean accept(final File dir, final String name) {
		        return name.matches(prefix + ".*");
		    }
		} );
		for (final File file : files) {
		    if (!file.delete() ) {
		        System.err.println("FileProcessor: Can't remove " + file.getAbsolutePath());
		    }
		}
	}
}