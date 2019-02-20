package cs455.overlay.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageNodeInputThread  implements Runnable{
	private ConcurrentLinkedQueue<Integer> queue;

	//Non blocking call
	public Integer get() throws InterruptedException {
		return queue.poll();
	}
	
	public MessageNodeInputThread() {
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
			   System.out.println("print-shortest-path");
			   System.out.println("exit-overlay");
			   continue;
		   } else if(command.toLowerCase().equals("quit") || command.toLowerCase().equals("kill")){
			   queue.add(-1);
			   return;
		   } else if(command.length() < 5){
			   System.out.println("Invalid command. Type 'HELP' for a list of valid commands.");
			   continue;
		   }
		
		   if(command.toLowerCase().equals("print-shortest-path")) {
			   System.out.println("Printing calculated paths:");
			   queue.add(1);
		   } else if(command.toLowerCase().equals("exit-overlay")) {
			   System.out.println("Exiting overlay");
			   queue.add(2);
		   } else {
			   System.out.println("Invalid command. Type 'HELP' for a list of valid commands.");
		   }
	    }	
	}

}
