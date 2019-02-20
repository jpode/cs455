package cs455.overlay.util;

import java.util.ArrayList;

import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.TrafficSummary;

public class StatisticsCollectorAndDisplay {
	ArrayList<String> table;
	
	public StatisticsCollectorAndDisplay() {
		table = new ArrayList<String>();
		table.add("           " + "\t" + "Num. Sent" + "\t" + "Num. Rec " + "\t" + "Sum. Sent" + "\t" + "Sum. Rec " + "\t" + "Num. Relayed");
	}
	
	private void addData(String node_addr, int num_sent, long sent_summation, int num_received, long received_summation, int num_relayed) {
		String new_row = new String(node_addr + "\t" + num_sent  + "\t\t" + num_received  + "\t" + sent_summation  + "\t" + received_summation  + "\t" + num_relayed);
		table.add(new_row);
	}

	public void display() {
		for(String row : table) {
			System.out.println(row);
		}
	}

	public void addData(Event e) {
		if(e.getType() == 9) {
			TrafficSummary summary = (TrafficSummary)e;
			String[] data = summary.getSplitData();
			String node_addr = data[1] + data[2];
			addData(node_addr, Integer.parseInt(data[3]), Long.parseLong(data[4]), Integer.parseInt(data[5]),  Long.parseLong(data[6]), Integer.parseInt(data[7]));
		}
		
	}

}
