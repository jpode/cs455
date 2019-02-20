package cs455.overlay.wireformats;

public class TrafficSummary implements Event{
	final int MESSAGE_TYPE = 9;
	String ip_address;
	int port_number;
	int num_sent;
	long sent_summation;
	int num_received;
	long received_summation;
	int num_relayed;
	
	public TrafficSummary(String ip, int port, int sent, long sentSum, int received, long recSum, int relayed) {
		ip_address = ip;
		port_number = port;
		num_sent = sent;
		sent_summation = sentSum;
		num_received = received;
		received_summation = recSum;
		num_relayed = relayed;
	}

	@Override
	public int getType() {
		return MESSAGE_TYPE;
	}

	@Override
	public byte[] getBytes() {
		return new String(MESSAGE_TYPE + "\n" + ip_address + "\n" + port_number + "\n" + num_sent + "\n" + sent_summation + "\n" + num_received + "\n" + received_summation + "\n" + num_relayed).getBytes();
	}

	@Override
	public String[] getSplitData() {
		return new String(MESSAGE_TYPE + "\n" + ip_address + "\n" + port_number + "\n" + num_sent + "\n" + sent_summation + "\n" + num_received + "\n" + received_summation + "\n" + num_relayed).split("\n");
	}
}
