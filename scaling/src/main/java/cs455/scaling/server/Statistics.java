package cs455.scaling.server;

public class Statistics {

	private final int client_id;
	private final int throughput;
	
	public Statistics(int client_id, int throughput) {
		this.client_id = client_id;
		this.throughput = throughput;
	}
	
	public int getID() {
		return client_id;
	}
	
	
	public int getThroughput() {
		return throughput;
	}
	
}
