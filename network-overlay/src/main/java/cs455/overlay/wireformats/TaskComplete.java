package cs455.overlay.wireformats;

public class TaskComplete {
	final int MESSAGE_TYPE = 7;
	String ip_address;
	int port_number;
	
	public TaskComplete(String ip, int port) {
		ip_address = ip;
		port_number = port;
	}
}
