package cs455.overlay.wireformats;

public class TaskInitiate {
	final int MESSAGE_TYPE = 5;
	int num_rounds;
	
	public TaskInitiate(int rounds) {
		num_rounds = rounds;
	}
}
