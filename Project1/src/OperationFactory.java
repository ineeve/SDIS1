
public class OperationFactory {

	public static Operation getOperation(String opStr) {
		switch (opStr) {
		case "BACKUP":
			return Operation.BACKUP;
		case "RESTORE":
			return Operation.RESTORE;
		case "DELETE":
			return Operation.DELETE;
		case "RECLAIM":
			return Operation.RECLAIM;
		default:
			return null;
		}
	}

}
