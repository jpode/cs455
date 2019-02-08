package cs455.overlay.wireformats;

public class RegisterRequest {
	final int MESSAGE_TYPE = 1;
	String ip_address;
	int port_number;
	
	public RegisterRequest(String ip, int port) {
		ip_address = ip;
		port_number = port;
	}

	public byte[] getPacket() {
		byte[] packet = new String(Integer.toString(MESSAGE_TYPE) + "\n" + ip_address + "\n" + Integer.toString(port_number)).getBytes();
		return packet;
	}
}
