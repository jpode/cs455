package cs455.overlay.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RegistryInputThread implements Runnable{
	private ConcurrentLinkedQueue<Integer> queue;

	//Non blocking call
	public Integer get() throws InterruptedException {
		return queue.poll();
	}
	
	public RegistryInputThread() {
		queue = new ConcurrentLinkedQueue<Integer>();
	}
	
	public void run() {
		//Listen for user commands
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String command = "";

	    while (true) {
			try {
				command = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			   
		   if(command.toLowerCase().equals("help")) {
			   System.out.println("list-messaging-nodes");
			   System.out.println("list-weights");
			   System.out.println("send-overlay-link-weights");
			   System.out.println("setup-overlay <number of connections>");
			   System.out.println("start <number of rounds>");
			   continue;
		   } else if(command.toLowerCase().equals("quit") || command.toLowerCase().equals("kill")){
			   queue.add(-1);
			   return;
		   } else if(command.length() < 5){
			   System.out.println("Invalid command. Type 'HELP' for a list of valid commands.");
			   continue;
		   }
		   
		   if(command.toLowerCase().equals("list-messaging-nodes")) {
			   System.out.println("Listing message nodes");
			   queue.add(1);
		   } else if(command.toLowerCase().equals("list-weights")) {
			   System.out.println("Listing node weights");
			   queue.add(2);
		   } else if(command.toLowerCase().equals("send-overlay-link-weights")) {
			   System.out.println("Sending overlay link weights");
			   queue.add(3);
		   } else if(command.length() > 13 && command.toLowerCase().substring(0,13).equals("setup-overlay")) {
			   if(command.length() < 15) {
				   System.out.println("Setting up overlay with default of 4 links per node...");
				   queue.add(4);
				   queue.add(4);
			   } else {
				   System.out.println("Setting up overlay...");
				   queue.add(4);
				   queue.add(Integer.parseInt(command.substring(14)));
			   }
		   	} else if(command.toLowerCase().substring(0,5).equals("start")) {
			   if(command.length() < 6) {
				   System.out.println("Starting overlay with default of 10 rounds");
				   queue.add(5);
				   queue.add(10);
			   } else {
				   System.out.println("Starting overlay");
				   queue.add(5);
				   queue.add(Integer.parseInt(command.substring(6)));
			   }
		   } else if(command.toLowerCase().substring(0,5).equals("reset")){
			   System.out.println("Resetting overlay");
			   queue.add(6);
		   } else {
			   System.out.println("Invalid command. Type 'HELP' for a list of valid commands.");
		   }
	    }
	}
}
