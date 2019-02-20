package cs455.overlay.wireformats;

public class MessagingNodeConnect implements Event{
	private final int MESSAGE_TYPE = 10;
	private String ip_address;
	private int port_number;
	
	public MessagingNodeConnect(String ip, int port){
		ip_address = ip;
		port_number = port;
	}

	public int getType() {
		return MESSAGE_TYPE;
	}
	
	public byte[] getBytes() {
		byte[] packet = new String(Integer.toString(MESSAGE_TYPE) + "\n" + ip_address + "\n" + port_number).getBytes();
		return packet;
	}

	public String[] getSplitData() {
		String[] result = new String[2];
		result[0] = ip_address;
		result[1] = Integer.toString(port_number);
		
		return result;
	}
}
