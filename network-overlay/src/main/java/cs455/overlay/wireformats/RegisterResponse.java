package cs455.overlay.wireformats;

public class RegisterResponse implements Event{
	final int MESSAGE_TYPE = 1;
	Byte status_code;
	String additional_info;
	
	public RegisterResponse(byte sc, String info) {
		status_code = sc;
		additional_info = info;
	}
	
	public int getType() {
		return MESSAGE_TYPE;
	}
	
	public byte[] getBytes() {
		return new String(Integer.toString(MESSAGE_TYPE) + "\n" + status_code.toString() + "\n" + additional_info).getBytes();
	}
}
