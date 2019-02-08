package cs455.overlay.wireformats;

public class DeregisterRequest {

	final int MESSAGE_TYPE = 3;
	String ip_address;
	int port_number;
	
	public DeregisterRequest(String ip, int port) {
		ip_address = ip;
		port_number = port;
	}
}
