package cs455.overlay.wireformats;

public interface Event {
	public int getType();
	public byte[] getBytes();
	public String[] getSplitData();
}
