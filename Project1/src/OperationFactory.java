
public class OperationFactory {

	public static Operation getOperation(String opStr) {
		switch (opStr) {
		case "BACKUP":
			return Operation.BACKUP;
		case "BACKUPENH":
			return Operation.BACKUPENH;
		case "RESTORE":
			return Operation.RESTORE;
		case "RESTOREENH":
			return Operation.RESTOREENH;
		case "DELETE":
			return Operation.DELETE;
		case "DELETEENH":
			return Operation.DELETEENH;
		case "RECLAIM":
			return Operation.RECLAIM;
		case "STATE":
			return Operation.STATE;
		default:
			return Operation.INVALID;
		}
	}

}
