import utils.FutureBuffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileProcessor{

    private static Scanner terminal = new Scanner(System.in);

    public static String createChunkName(String fileId, Integer chunkNo){
        return fileId + "_" + chunkNo + ".out";
    }

    /**
     * Writes a file asynchronously
     * @param path output path to write the file into
     * @param chunksToWrite ArrayList with all the chunks that make the file
     * @param chunkSizes Max size of each chunk
     * @return true if file is being created, false otherwise.
     */
    public static ArrayList<Future<Integer>> writeFileAsync(Path path, ArrayList<byte[]> chunksToWrite, int chunkSizes){
        AsynchronousFileChannel fileChannel = null;
        try {
            fileChannel = AsynchronousFileChannel.open(path,StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        ArrayList<Future<Integer>> futures = new ArrayList<>();
        if (chunksToWrite.size() > 0){
            long position = 0;
            for (int i = 0; i < chunksToWrite.size(); i++){
                byte[] chunkToWrite = chunksToWrite.get(i);
                ByteBuffer buffer = ByteBuffer.allocate(chunkSizes);
                buffer.put(chunkToWrite);
                buffer.flip();
                futures.add(fileChannel.write(buffer, position));
                position += chunkToWrite.length;
                buffer.clear();
            }
            return futures;
        }
        return null;
    }

    public static Future<Integer> writeSingleChunkAsync(Path path, byte[] chunkToWrite){
        AsynchronousFileChannel fileChannel = null;
        try {
            fileChannel = AsynchronousFileChannel.open(path,StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("Storing chunk with length: " + chunkToWrite.length);
        ByteBuffer buffer = ByteBuffer.allocate(chunkToWrite.length);
        long position = 0;
        buffer.put(chunkToWrite);
        buffer.flip();
        Future<Integer> future = fileChannel.write(buffer, position);
        buffer.clear();

        return future;
    }

    public static ArrayList<FutureBuffer> readFileChunksAsync(File file){
        int chunkSize = Config.MAX_CHUNK_SIZE;
        int numChunks = (int) Math.ceil(file.length() / (double)chunkSize);
        ArrayList<FutureBuffer> futureBuffers = new ArrayList<>(numChunks);
        AsynchronousFileChannel fileChannel;
        Path path;
        try {
            path = Paths.get(file.getCanonicalPath());
        } catch (IOException e) {
            System.out.println("I do not have file: " + file.getName());
            return null;
        }
        try {
            fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int bufferPosition = 0;
        for(int i = 0; i < numChunks; i++){
            ByteBuffer newByteBuffer = ByteBuffer.allocate(chunkSize);
            Future<Integer> newFuture = fileChannel.read(newByteBuffer, bufferPosition);
            futureBuffers.add(new FutureBuffer(newByteBuffer,newFuture));
            bufferPosition += chunkSize;
        }
        return futureBuffers;
    }


    public static byte[] getDataFromFuture(FutureBuffer futureBuffer){
        ByteBuffer buffer = futureBuffer.getBuffer();
        try {
            futureBuffer.getFuture().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        buffer.clear();
        return data;
    }

    public static FutureBuffer getDataAsync(File file){
		Path path;
        AsynchronousFileChannel fileChannel;
		try {
			path = Paths.get(file.getCanonicalPath());
		} catch (IOException e) {
			System.out.println("I do not have file: " + file.getName());
			return null;
		}
        try {
            fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        } catch (IOException e) {
		    e.printStackTrace();
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate((int)file.length());
		Future<Integer> future = fileChannel.read(buffer,0);
		return new FutureBuffer(buffer,future);
    }


    public static File loadFile(String filepath){
        File file = new File(filepath);
        if (!file.exists()){
            return null;
        }
        if (!file.canRead()){
            System.out.println("Can not read file " + filepath);
            return null;
        }
        return file;
    }

    public static String getFileId(File file){
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
	public static String getFileIdByPath(Path filepath){
    	String filename = filepath.getFileName().toString();
    	return filename.split("_")[0];
	}
	public static Integer getChunkNo(Path filepath){
        String filename = filepath.getFileName().toString();
        return Integer.valueOf(filename.split("_")[1].split("\\.")[0]);
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