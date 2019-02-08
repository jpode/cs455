package cs455.overlay.registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/* The Controller class is the foreground registry process
 * It is responsible for creating/starting the server and registry objects, and listening for user commands
 * User commands are parsed and correspond to method in the Registry object
 * 
 */
public class Controller {
	
	private static Server server;
	private static Registry reg =  Registry.getInstance();
	
	public static void main(String[] args) throws IOException {
		server = new Server(5001, 10);
		
		System.out.println("Starting server...");
		server.startServer();
		System.out.println("Server started");

		//Listen for user commands
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String command = "";

	    while (true) {
		   command = in.readLine();
		   
		   if(command.toLowerCase().equals("help")) {
			   System.out.println("list-messaging-nodes");
			   System.out.println("list-weights");
			   System.out.println("send-overlay-link-weights");
			   System.out.println("setup-overlay <number of connections>");
			   System.out.println("start <number of rounds>");
			   continue;
		   } else if(command.toLowerCase().equals("quit")){
			   server.killServer();
			   return;
		   } else if(command.length() < 5){
			   System.out.println("Invalid command. Type 'HELP' for a list of valid commands.");
			   continue;
		   }
		   
		   if(command.toLowerCase().equals("list-messaging-nodes")) {
			   System.out.println("Listing message nodes");
			   System.out.println(reg.listMessageNodes());
		   } else if(command.toLowerCase().equals("list-weights")) {
			   System.out.println("Listing node weights");
			   System.out.println(reg.listWeights());
		   } else if(command.toLowerCase().equals("send-overlay-link-weights")) {
			   System.out.println("Sending overlay link weights");
			   reg.sendOverlayLinkWeights();
		   } else if(command.toLowerCase().substring(0,5).equals("start")) {
			   if(command.length() < 6) {
				   reg.start("10");
				   System.out.println("Starting overlay with default of 10 rounds");
			   } else {
				   reg.start(command.substring(6));
				   System.out.println("Starting overlay");
			   }
		   } else if(command.toLowerCase().substring(0,13).equals("setup-overlay")) {
			   if(command.length() < 15) {
				   reg.constructOverlay("4");
				   System.out.println("Setting up overlay with default of 4 links per node");
			   } else {
				   reg.constructOverlay(command.substring(14));
				   System.out.println("Setting up overlay");
			   }
		   } else {
			   System.out.println("Invalid command. Type 'HELP' for a list of valid commands.");
		   }
	   }
	}
}
