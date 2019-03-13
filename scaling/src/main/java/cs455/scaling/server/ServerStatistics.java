package cs455.scaling.server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ServerStatistics implements Runnable{

	//Container for and ID and total current period throughput for all clients that have sent at least one message during the current period
	private HashMap<Integer, Integer> clients;
	
	//Total throughput of the server during the current period
	private double throughput;
	//Number of connections that have sent messages during the current period
	private int active_connections;
	//Mean per-second throughput per-client for the current period
	private double mean_pc_throughput;
	//Standard deviation of throughput per-second per-client for the current period
	private double std_dev_pc_throughput;
	//Timestamp format: Hours:Minutes:Seconds
	private DateTimeFormatter date_format;
	
	
	public ServerStatistics() {
		clients = new HashMap<Integer, Integer>();
		throughput = 0;
		active_connections = 0;
		mean_pc_throughput = 0;
		date_format = DateTimeFormatter.ofPattern("hh:mm:ss");
	}
	 
	public synchronized void addClientThroughput(Integer[] stat_ids) {
		if(stat_ids != null) {
			for(int i = 0; i < stat_ids.length; i++) {
				
				//Check if statistics for the current client have already been added, then append the new statistics
				if(clients.containsKey(stat_ids[i])) {
					clients.put(stat_ids[i], clients.get(stat_ids[i]).intValue() + 1);
				} else {
					clients.put(stat_ids[i], 1);
					active_connections++;
				}
				
				throughput++;
				mean_pc_throughput = (throughput / 20) / active_connections;
			}
		}
	}
	

	
	private synchronized void printStatistics() {
		calculateStdDev();
		
		LocalTime time = LocalTime.now();
		String formatted_time = time.format(date_format);
		
		String statistics_output = "[" + formatted_time + "]";
		statistics_output += " Server Throughput: " + throughput / 20 + " messages/s,";
		statistics_output += " Active Client Connections: " + active_connections + ",";
		statistics_output += " Mean Per-client Throughput: " + mean_pc_throughput + " messages/s, ";
		statistics_output += " Std. Dev. Of Per-client Throughput: " + std_dev_pc_throughput + " messages/s";
		
		System.out.println(statistics_output);
		
		//Reset statistics
		clients.clear();
		throughput = 0;
		active_connections = 0;
		mean_pc_throughput = 0;
	}
	
	private void calculateStdDev() {
		double sum = 0;
		
		Iterator<Entry<Integer, Integer>> client = clients.entrySet().iterator();
		
		while(client.hasNext()) {
			Map.Entry<Integer, Integer> x = (Map.Entry<Integer, Integer>)client.next();
			sum += Math.pow(((double)x.getValue() / 20) - mean_pc_throughput, 2);
		}
		std_dev_pc_throughput = Math.sqrt(sum / active_connections);
	}
	
	@Override
	public void run() {

		while(true) {
			try {
				//Print the statistics every 20 seconds
				Thread.currentThread().sleep(20000);
				this.printStatistics();
				
			} catch (InterruptedException e) {
				//Server is being shut down, print output before exiting the run method
				System.out.println("FINAL OUTPUT:\n");
				this.printStatistics();

				return;
			}
		}
	}

}
