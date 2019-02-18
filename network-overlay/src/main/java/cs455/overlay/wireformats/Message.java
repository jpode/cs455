package cs455.overlay.wireformats;

public class Message implements Event{
	private final int MESSAGE_TYPE = 6;
	private int payload;
	private String path;
	
	public Message(int payload, String path) {
		this.payload = payload;
		this.path = path;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
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
