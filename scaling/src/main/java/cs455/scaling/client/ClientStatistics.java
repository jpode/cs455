package cs455.scaling.client;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientStatistics implements Runnable{

	private int send_count;
	private int receive_count;
	private DateTimeFormatter date_format;

	public ClientStatistics() {
		send_count = 0;
		receive_count = 0;
		date_format = DateTimeFormatter.ofPattern("hh:mm:ss");
	}
	
	public synchronized void updateSendCount() {
		send_count++;
	}
	
	public synchronized void updateReceiveCount() {
		receive_count++;
	}
	
	private synchronized void printStatistics() {
		
		LocalTime time = LocalTime.now();
		String formatted_time = time.format(date_format);
		
		String statistics_output = "[" + formatted_time + "]";
		statistics_output += " Total Sent Count: " + send_count + ",";
		statistics_output += " Total Received Count: " + receive_count;
		
		System.out.println(statistics_output);
		
		//Reset statistics
		send_count = 0;
		receive_count = 0;
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
