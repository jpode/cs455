package cs455.overlay.wireformats;

public class TaskComplete implements Event{
	final int MESSAGE_TYPE = 7;
	String ip_address;
	int port_number;
	
	public TaskComplete(String ip, int port) {
		ip_address = ip;
		port_number = port;
	}

	@Override
	public int getType() {
		return MESSAGE_TYPE;
	}

	@Override
	public byte[] getBytes() {
		return new String(Integer.toString(MESSAGE_TYPE) + "\n" + ip_address + "\n" + port_number).getBytes();
	}

	@Override
	public String[] getSplitData() {
		return new String(MESSAGE_TYPE + "\n" + ip_address + "\n" + port_number).split("\n");
	}
}
