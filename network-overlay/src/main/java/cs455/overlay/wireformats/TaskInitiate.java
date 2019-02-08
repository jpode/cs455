package cs455.overlay.wireformats;

public class TaskInitiate {
	final int MESSAGE_TYPE = 6;
	int num_rounds;
	
	public TaskInitiate(int rounds) {
		num_rounds = rounds;
	}
}
