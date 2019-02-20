package cs455.overlay.node;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Connection;
import cs455.overlay.util.NodeRepresentation;
import cs455.overlay.util.OverlayCreator;
import cs455.overlay.util.RegistryInputThread;
import cs455.overlay.util.ServerListenerThread;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

/* Registry class provides four functions:
 * 	Register nodes
 *	De-register nodes
 * 	Construct overlay connections between nodes
 * 	Assign link weights
 * 
 * 
 * To achieve this, the Registry class creates three threads and continually monitors the queues of each for new events:
 * 	Server thread to listen for new connections
 * 	Input thread to listen for user inputs
 * 	Connection listener to listen for messages from any of the registered messaging nodes
 */
public class Registry {
	//Registry data structure. Node representations are used by the server to store data about registered nodes
	private ArrayList<NodeRepresentation> node_registry;
	//List of connections between nodes for use by the registry. Instantiated upon overlay construction
	private Connection[] connection_list;
	//Thread to listen for new messages from registered nodes
	private ServerListenerThread server_listener;
	//Thread to listen for user inputs
	private RegistryInputThread input_listener;
	//Overlay creator object
	private OverlayCreator overlay;
	//Indicates that the overlay has been created for the current session
	private boolean overlay_constructed;
	//Number of tasks completed for the current message session
	private int tasks_completed;
	private int statistics_received;
	//For summary statistics
	private StatisticsCollectorAndDisplay statistics;
	
	public Registry() {
		node_registry = new ArrayList<NodeRepresentation>();
		statistics = new StatisticsCollectorAndDisplay();
		overlay_constructed = false;
		tasks_completed = 0;
		statistics_received = 0;
	}
	
	private void onEvent(Event e) {
		//System.out.println("Registry::onEvent: received new event");
		switch(e.getType()) {
			case(2): //Deregister request
				//Register the new node and send registration response to the node
				NodeRepresentation node = new NodeRepresentation(e.getSplitData()[0], Integer.parseInt(e.getSplitData()[1]));
				if(node_registry.contains(node)) {
					TCPSender response_sender = new TCPSender(node_registry.get(node_registry.indexOf(node)).getSocket());
	
					if(deregister(node)){
						//System.out.println("Sending deregistration response to node");
						response_sender.sendEvent(EventFactory.getInstance().createEvent("1" + "\n" + "2" + "\n" + "Deregistration successful"));
						
						server_listener.closeConnection(node_registry.get(node_registry.indexOf(node)).getSocket());
						node_registry.get(node_registry.indexOf(node)).kill();
						node_registry.remove(node);

					} else {
						//System.out.println("Sending deregistration response to node");
						response_sender.sendEvent(EventFactory.getInstance().createEvent("1" + "\n" + "3" + "\n" + "Dergistration unsuccessful"));
					}
				}
				break;
			case(7): //TaskComplete
				tasks_completed++;
				break;
			case(9): //TrafficSummary
				
				statistics.addData(e);
				statistics_received++;
				
				if(statistics_received == node_registry.size()) {
					statistics.display();
					//Reset statistics collector
					statistics = new StatisticsCollectorAndDisplay();
				}
				
				break;
			default:
				//System.out.println("ERR::Registry::onEvent: invalid message type for registry");
				return;
		}
	}
	
	//Overloaded method is required for request events, because a new socket is associated with the event
	private void onEvent(Event e, Socket socket, TCPReceiverThread rec, Thread thd) {
		String[] data_lines;
		
		if(e.getBytes() == null) {
			//System.out.println("ERR: received message is null, registration failed");
			return;
		} else {
			 data_lines = new String(e.getBytes()).split("\n");
		}
		
		//Create a new node representation out of the IP address, port, and socket
		NodeRepresentation new_node = new NodeRepresentation(data_lines[1], Integer.parseInt(data_lines[2]), socket);

		TCPSender response_sender = new TCPSender(socket);
		
		//Register the new node and send registration response to the node
		if(register(new_node, rec, thd)) {
			//System.out.println("Sending registration response to node");
			System.out.println("Registered new node " + new_node.toString());
			response_sender.sendEvent(EventFactory.getInstance().createEvent("1" + "\n" + "0" + "\n" + "Registration successful"));
		} else {
			//System.out.println("Sending registration response to node");
			response_sender.sendEvent(EventFactory.getInstance().createEvent("1" + "\n" + "1" + "\n" + "Registration unsuccessful"));
			
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	//Request to register node. Fails if node is already registered, otherwise registers
	private boolean register(NodeRepresentation node, TCPReceiverThread rec, Thread thd) {
		for(NodeRepresentation check_node : node_registry) {
			if(node.equals(check_node)) {
				//System.out.println("ERR::Registry::register: registration of new node unsuccessful as node is already registered");
				//System.out.println("\t Registered Node: " + check_node.toString());
				//System.out.println("\t Duplicated Node: " + node.toString());
				return false;
			}
		}
		
		node_registry.add(node);
		//server_listener.addConnection(node.getSocket(), node.getDataInputStream());
		server_listener.addConnection(rec, thd);
		//System.out.println("Registry::register: registration of new node successful");
		return true;
	}
	
	//Request to deregister node. Fails if node is not registered, otherwise deregisters
	private boolean deregister(NodeRepresentation node) {
		if(!overlay_constructed) {
			for(NodeRepresentation check_node : node_registry) {
				if(node.equals(check_node)) {
					System.out.println("Deregistered node " + check_node.toString());
					return true;
				}
			}
		}
		
		return false;
		//System.out.println("WRN::Registry::register: node unable to be deregistered");
	}

	private void listMessageNodes() {
		String output = "";
		
		for(NodeRepresentation node : node_registry) {
			output += node.toString() + "\n";
		}
		
		System.out.println(output);
	}

	private void listWeights() {
		if(!overlay_constructed) {
			System.out.println("ERR:Registry:Overlay not yet constructed." );
			return;
		}
		String output = "";
		
		for(Connection conn : connection_list) {
			output += conn.toString() + "\n";
		}	
		
		System.out.println("Number of connections: " + connection_list.length);
		System.out.println(output);
		
	}

	private void sendOverlayLinkWeights() {
		String data = "4" + "\n" + connection_list.length + "\n";
		
		for(Connection conn : connection_list) {
			data += conn.toString() + "\n";
		}
		
		messageAllNodes(EventFactory.getInstance().createEvent(data));
	}

	private void setupOverlay(int num_connections) {
		overlay = new OverlayCreator();
		System.out.println("Registry: attempting to construct the overlay...");
		connection_list = overlay.constructOverlay(node_registry, num_connections);
		overlay_constructed = true;
		System.out.println("Registry: Overlay constructed. Server will not add or remove nodes from this overlay until overlay is reset.");
	}
	
	private void start(int num_rounds) {
		messageAllNodes(EventFactory.getInstance().createEvent("5" + "\n" + num_rounds));	
	}
	
	private void start_listening(int port) {
		TCPServerThread server = new TCPServerThread(port);
		input_listener = new RegistryInputThread();
		server_listener = new ServerListenerThread();
		
		//Start threads
		Thread server_thread = new Thread(server);
		server_thread.start();
		
		Thread input_thread = new Thread(input_listener);
		input_thread.start();
		
		Thread node_thread = new Thread(server_listener);
		node_thread.start();
		
		Socket new_connection;
		Integer new_input;
		Event new_event;
		TCPReceiverThread new_receiver;
		Thread new_thread;
		//The get() calls in the while loop do not block, because the registry needs to be actively listening for updates from all three threads
		while(true) {
			try {
				if(!overlay_constructed) { //If the overlay is constructed, don't try to register new nodes
					//Check if there is a new connection by getting its socket
					new_connection = server.getSocket();
					if(new_connection != null) {
						//System.out.println("Registry::start_listening: new connection socket found");
						//If there is, get the associated event, active thread, and receiver
						new_event = server.getEvent();
						new_receiver = server.getReciever();
						new_thread = server.getThread();
						onEvent(new_event, new_connection, new_receiver, new_thread);
					}
				}
				//Check if there are any user inputs
				new_input = input_listener.get();
				if(new_input != null) {
					switch(new_input) {
						case(0): //Kill the registry
							server.killServer();
							server_thread.interrupt();
							
							input_thread.interrupt();
							
							server_listener.kill();
							node_thread.interrupt();
							return;
						case(1): //List messaging nodes
							listMessageNodes();
							break;
						case(2): //List link weights
							if(overlay_constructed) {
								listWeights();
							} else {
								System.out.println("Overlay not constructed");
							}
							break;
						case(3): //Send link weights to overlay
							if(overlay_constructed) {
								sendOverlayLinkWeights();
							} else {
								System.out.println("Overlay not constructed");
							}
							break;
						case(4): //Set up overlay
							
							if(!overlay_constructed) {
								if(node_registry.size() < 3) {
									System.out.println("ERR:Registry: not enough connected nodes to set up overlay");
									input_listener.get(); //Clear the message queue
								} else {
									//System.out.println("Registry: received setup overlay command");
									setupOverlay(input_listener.get()); //Another get() is called to retrieve number of connections
								}
							} else {
								System.out.println("Overlay already constructed");
							}
							break;
						case(5): //Start messaging
							start(input_listener.get()); //Another get() is called to retrieve number of messaging rounds
							break;
						case(6):
							reset();
							break;
					}
				}
				
				//Check if there are any new messages from existing connections
				new_event = server_listener.get();
				if(new_event != null) {
					onEvent(new_event);
				}
				
				//Check if the number of tasks completed equals the size of the registry, meaning the message session is done
				if(tasks_completed == node_registry.size()) {
					//Sleep for 15 seconds to ensure that all messages get delivered
					Thread.sleep(15000);
					messageAllNodes(EventFactory.getIip
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void messageAllNodes(Event e) {
		for(NodeRepresentation node : node_registry) {
				TCPSender sender = new TCPSender(node.getSocket());
				sender.sendEvent(e);
		}
	}
	
	//Reset everything back to before the 'setup-overlay x' command was issued
	private void reset() {
		System.out.println("Resetting registry...");

		for(NodeRepresentation node : node_registry) {
			node.resetConnections();
		}
		
		connection_list = null;
		overlay_constructed = false;
		tasks_completed = 0;
		statistics_received = 0;
		
		System.out.println("Registry reset");
	}
	
	public static void main(String[] args){
		if(args.length >= 1) {
			Registry registry = new Registry();
			System.out.println("Registry node starting");
			registry.start_listening(Integer.parseInt(args[0]));
		} else {
			System.out.println("ERR: must specify port for registry to start on");
		}
	}

	 
}
