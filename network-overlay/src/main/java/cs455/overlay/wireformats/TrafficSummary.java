package cs455.overlay.wireformats;

public class TrafficSummary {
	final int MESSAGE_TYPE = 9;
	String ip_address;
	int port_number;
	int num_sent;
	String sent_summation;
	int num_received;
	String received_summation;
	int num_relayed;
	
	public TrafficSummary(String ip, int port, int sent, String sentSum, int received, String recSum, int relayed) {
		ip_address = ip;
		port_number = port;
		num_sent = sent;
		sent_summation = sentSum;
		num_received = received;
		received_summation = recSum;
		num_relayed = relayed;
	}
}
