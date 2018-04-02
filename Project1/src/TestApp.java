

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {
	
	private static String hostname;
	private static String peerId;
	private static Operation operation;
	private static String pathname; // for BACKUP, RESTORE and DELETE
	private static long maxDiskSpace; // KByte for RECLAIM
	private static byte desiredRepDegree; // for BACKUP
	
	public static void main(String[] args) {
		parseArgs(args);
		try {
			// Getting the registry 
	        Registry registry = LocateRegistry.getRegistry(hostname); 
	    
	        // Looking up the registry for the remote object 
	        RMIInterface stub = (RMIInterface) registry.lookup(peerId); 
	    
	        // Calling the remote method using the obtained object 
	        invokeServer(stub);
	    } catch (Exception e) {
	        System.err.println("Client exception: " + e.toString());
	        e.printStackTrace();
	    } 
	}

	private static void invokeServer(RMIInterface stub) throws RemoteException {
		switch (operation) {
		case BACKUP:
			stub.backup(pathname, desiredRepDegree);
			break;
		case BACKUPENH:
			stub.backupEnh(pathname, desiredRepDegree);
			break;
		case RESTORE:
			stub.restore(pathname);
			break;
		case DELETE:
			stub.delete(pathname);
			break;
        case DELETEENH:
            stub.deleteEnh(pathname);
            break;
		case RECLAIM:
			stub.reclaim(maxDiskSpace);
		case STATE:
			PeerState state = stub.state();
			state.present();
			break;
		case RESTOREENH:
			stub.restoreEnh(pathname);
			break;
		default:
			System.err.println("TestApp: Invalid Operation.");
		}
	}

	private static void parseArgs(String[] args) {
		if (args.length < 2) {
			printUsage();
			System.exit(1);
		}
		parseAccessPoint(args[0]);
		operation = OperationFactory.getOperation(args[1]);
		
		switch (operation) {
		case BACKUP:
		case BACKUPENH:
		case RESTORE:
		case RESTOREENH:
		case DELETE:
		case DELETEENH:
			if (args.length < 3) {
				printUsage();
				System.exit(1);
			}
			pathname = args[2];
			break;
		case RECLAIM:
			if (args.length < 3) {
				printUsage();
				System.exit(1);
			}
			maxDiskSpace = Long.parseLong(args[2]);
			break;
		case STATE:
			break;
		default:
			System.err.println("TestApp: Invalid Operation.");
			printUsage();
			System.exit(1);
		}
		
		if (operation == Operation.BACKUP || operation == Operation.BACKUPENH) {
			if (args.length != 4) {
				printUsage();
				System.exit(1);
			}
			desiredRepDegree = (byte) Integer.parseInt(args[3]);
		}
	}

	// <IP>//<PeerID>
	private static void parseAccessPoint(String arg) {
		String[] args = arg.split("//");
		hostname = args[0];
		peerId = args[1];
	}

	private static void printUsage() {
		System.out.println("java TestApp <access_point> <operation> <opnd_1> <opnd_2>");
	}

}
