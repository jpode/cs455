package cs455.scaling.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

public class StatisticsTester {

	
	private static void calculateStdDev(HashMap<Integer, Integer> clients, double mean_pc_throughput, int active_connections) {
		double sum = 0;
		
		Iterator<Entry<Integer, Integer>> client = clients.entrySet().iterator();
		
		while(client.hasNext()) {
			System.out.println("\t" + sum);

			Map.Entry<Integer, Integer> x = (Map.Entry<Integer, Integer>)client.next();
			sum += Math.pow(((double)x.getValue() / 20) - mean_pc_throughput, 2);
			//System.out.println(Math.pow(((double)x.getValue() / 20) - mean_pc_throughput, 2));
		}
		System.out.println(sum / active_connections);
		System.out.println(Math.sqrt(sum / active_connections));
	}
	
	
	public static void main(String[] args) {
		ServerStatistics stats = new ServerStatistics();
		Thread stats_thread = new Thread(stats);
		stats_thread.start();
		Random randomNum = new Random();
		
		int counter = 0;
		int counter_2 = 0;
		while(counter_2 < 100) {
			if(counter == 50) {
				counter = 0;
			}

			Statistics test = new Statistics(counter, 40);
			//stats.addClientThroughput(test);
			//Thread.currentThread().sleep((long)(randomNum.nextInt(70) + 30));
			counter++;
			counter_2++;
		}
		/*
		HashMap<Integer, Integer> testmap = new HashMap<Integer, Integer>();
		testmap.put(0, 57);
		testmap.put(1, 61);
		testmap.put(2, 60);
		testmap.put(3, 62);
	
		calculateStdDev(testmap, 3, 4);
*/
		
	}

}
