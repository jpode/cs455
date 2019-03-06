package cs455.overlay.wireformats;

public class TaskInitiate implements Event{
	final int MESSAGE_TYPE = 5;
	int num_rounds;
	
	public TaskInitiate(int rounds) {
		num_rounds = rounds;
	}

	public int getType() {
		return MESSAGE_TYPE;
	}

	@Override
	public byte[] getBytes() {
		return new String(MESSAGE_TYPE + "\n" + num_rounds).getBytes();
	}

	@Override
	public String[] getSplitData() {
		return new String(MESSAGE_TYPE + "\n" + num_rounds).split("\n");
	}
}
