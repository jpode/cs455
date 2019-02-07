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
	
	private static Registry reg;
	private static Server server;
	
	public static void main(String[] args) throws IOException {
		reg = new Registry(4);
		server = new Server(reg, 5001, 10);
		
		System.out.println("Starting server...");
		server.startServer();
		System.out.println("Server started");

		//Listen for user commands
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String command = "";

	    while (command.equalsIgnoreCase("quit") == false) {
		   command = in.readLine();
		   
		   if(command.toLowerCase().equals("list-message-nodes")) {
			   reg.listMessageNodes();
		   } else if(command.toLowerCase().equals("list-weights")) {
			   reg.listWeights();
		   } else if(command.toLowerCase().equals("send-overlay-link-weights")) {
			   reg.sendOverlayLinkWeights();
		   } else if(command.toLowerCase().substring(0,5).equals("start")) {
			   reg.start(command.substring(6));
		   } else if(command.toLowerCase().substring(0,13).equals("setup-overlay")) {
			   reg.start(command.substring(14));
		   } else if(command.toLowerCase().equals("help")) {
			   System.out.println("list-message-nodes");
			   System.out.println("list-weights");
			   System.out.println("send-overlay-link-weights");
			   System.out.println("setup-overlay <number of connections>");
			   System.out.println("start <number of rounds>");
		   } else {
			   System.out.println("Invalid command. Type 'HELP' for a list of valid commands.");
		   }
	   }
	}
}
