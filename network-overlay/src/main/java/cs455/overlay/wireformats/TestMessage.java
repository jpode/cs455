package cs455.overlay.wireformats;

public class TestMessage implements Event{
	private final int MESSAGE_TYPE = 11;

	@Override
	public int getType() {
		return  MESSAGE_TYPE;
	}

	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return new String(Integer.toString(MESSAGE_TYPE)).getBytes();
	}

	@Override
	public String[] getSplitData() {
		// TODO Auto-generated method stub
		return new String(Integer.toString(MESSAGE_TYPE)).split("\n");
	}

}
