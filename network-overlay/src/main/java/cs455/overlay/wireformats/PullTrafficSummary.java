package cs455.overlay.wireformats;

public class PullTrafficSummary implements Event {
	final int MESSAGE_TYPE = 8;
	
	public PullTrafficSummary() {}

	@Override
	public int getType() {
		return MESSAGE_TYPE;
	}

	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return new String(Integer.toString(MESSAGE_TYPE)).getBytes();
	}

	@Override
	public String[] getSplitData() {
		// TODO Auto-generated method stub
		return new String(Integer.toString(MESSAGE_TYPE)).split("\n"); //Will be an array of size 1
	}
}
