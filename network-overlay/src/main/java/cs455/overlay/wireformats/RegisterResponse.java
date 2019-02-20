package cs455.overlay.wireformats;

public class RegisterResponse implements Event{
	final int MESSAGE_TYPE = 1;
	Byte status_code;
	String additional_info;
	
	public RegisterResponse(byte sc, String info) {
		status_code = sc;
		additional_info = info;
	}
	
	@Override
	public int getType() {
		return MESSAGE_TYPE;
	}
	
	@Override
	public byte[] getBytes() {
		return new String(Integer.toString(MESSAGE_TYPE) + "\n" + new String(new byte[] {status_code}) + "\n" + additional_info).getBytes();
	}

	@Override
	public String[] getSplitData() {
		return (new String(Integer.toString(MESSAGE_TYPE) + "\n" + new String(new byte[] {status_code}) + "\n" + additional_info)).split("\n");
	}
	
	public int getStatusCode() {
		//Converting to string first ensures the correct int value is returned
		return (Integer.parseInt(new String(new byte[] {status_code})));
	}
}
