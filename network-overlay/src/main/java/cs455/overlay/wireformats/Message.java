package cs455.overlay.wireformats;

public class Message implements Event{
	private final int MESSAGE_TYPE = 6;
	private int payload;
	private String path;
	
	public Message(String path, int payload) {
		this.path = path;
		this.payload = payload;
	}

	@Override
	public int getType() {
		return MESSAGE_TYPE;
	}

	@Override
	public byte[] getBytes() {
		return (new String(MESSAGE_TYPE + "\n" + path + "\n" + payload)).getBytes();
	}

	@Override
	public String[] getSplitData() {
		return new String(MESSAGE_TYPE + "\n" + path + "\n" + payload).split("\n");
	}
}
