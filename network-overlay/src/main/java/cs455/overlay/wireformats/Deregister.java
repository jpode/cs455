package cs455.overlay.wireformats;

public class Deregister implements Event{

	private final int MESSAGE_TYPE = 3;
	private String ip_address;
	private int port_number;
	
	
	public Deregister(String ip, int port) {
		ip_address = ip;
		port_number = port;
	}

	public int getType() {
		return MESSAGE_TYPE;
	}

	public byte[] getBytes() {
		byte[] packet = new String(Integer.toString(MESSAGE_TYPE) + "\n" + ip_address + "\n" + Integer.toString(port_number)).getBytes();
		return packet;
	}
}
