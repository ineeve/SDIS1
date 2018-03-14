
public class PutchunkData {
	private String peerId;
	private String fileId;
	private byte chunkNo;
	private String data;

	PutchunkData(String peerId, String fileId, byte chunkNo, String data){
		this.setPeerId(peerId);
		this.setFileId(fileId);
		this.setChunkNo(chunkNo);
		this.setData(data);
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public byte getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(byte chunkNo) {
		this.chunkNo = chunkNo;
	}
}
