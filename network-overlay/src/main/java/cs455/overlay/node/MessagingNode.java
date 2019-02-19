package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.djikstra.ShortestPath;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Connection;
import cs455.overlay.util.MessageNodeInputThread;
import cs455.overlay.util.NodeRepresentation;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.Message;

public class MessagingNode implements Node{
	
	//Socket for connection to registry node
	private Socket registry_socket;
	//Runnable object to listen for messages from the registry
	private TCPReceiverThread registry_listener;
	//Sender object to send messages to registry
	private TCPSender registry_sender;
	//Thread that the registry_listener will run in
	private Thread registry_thread;
	//Name of this node
	private String self;
	//Port that this node is listening on for connections from other nodes - this will be sent to the registry
	private int listening_port;
	//Runnable object to listen for connections from other nodes
	private TCPServerThread node_listener;
	//Thread that the node_listener will run in
	private Thread node_thread;
	//Thread to listen for user inputs
	private MessageNodeInputThread input_listener;
	//List of nodes that this node directly connects to. Format: <ip>:<port>
	private String[] connections;
	//List of all connections in the overlay with weights
	private Connection[] all_connections;
	//List of all nodes in the overlay
	//This is a HashSet because the total number of nodes in the overlay is not sent by the registry, and it avoids duplicates
	private HashSet<NodeRepresentation> all_nodes; 
	//Object to calculate the shortest path and provide the starting node
	private ShortestPath route_calculator;
	//Metric tracking values
	private int sendTracker;
	private int receiveTracker;
	private int relayTracker;
	private double summation;
	
	public MessagingNode() {
		all_nodes = new HashSet<NodeRepresentation>();
		sendTracker = 0;
		receiveTracker = 0;
		relayTracker = 0;
		summation = 0;
	}
	
	public void onEvent(Event e) {
		String[] message;
		switch(e.getType()) {
			case(1): //RegisterrResponse
				if(((RegisterResponse)e).getStatusCode() == 0) {
					System.out.println("MessagingNode::connectToServer: registration successful");
					message = ((RegisterResponse)e).getSplitData();
					for(String line : message) {
						System.out.println(line);
					}
				} else {
					System.out.println("ERR:MessagingNode::connectToServer: registration not successful; status code = " + ((RegisterResponse)e).getStatusCode());
					registry_listener.kill();
					registry_thread.interrupt();
				}
				break;
			case(3)://MessagingNodesList
				message = ((MessagingNodesList)e).getSplitData();
				connections = new String[Integer.parseInt(message[1])];
				for(int i = 2; i < message.length; i++) {
					connections[i-2] = message[i];
				}
				break;
			case(4)://LinkWeights
				message = ((LinkWeights)e).getSplitData();
				String[] connection_split;
				all_connections = new Connection[Integer.parseInt(message[1])];
				
				for(int i = 2; i < message.length; i++) {
					//Format of message connections: <ip 1>:<port 1> <ip 2>:<port 2> <weight>
					connection_split = message[i].split("\\s+");
					NodeRepresentation node_1 = new NodeRepresentation(connection_split[0].split(":")[0], Integer.parseInt(connection_split[0].split(":")[1]));
					NodeRepresentation node_2 = new NodeRepresentation(connection_split[1].split(":")[0], Integer.parseInt(connection_split[1].split(":")[1]));
					all_connections[i-2] = new Connection(node_1, node_2, Integer.parseInt(connection_split[2]));
					all_nodes.add(node_1);
					all_nodes.add(node_2);
				}
				break;
			case(5)://TaskInitiate
				message = ((TaskInitiate)e).getSplitData();
				messageNodes(Integer.parseInt(message[1]));
				break;
			case(6)://Message
				message = ((Message)e).getSplitData();
				String[] path = message[1].split("--");
				if(path[path.length-1].equals(self)) {
					//Message has reached it's destination
					System.out.println("Node received message from " + path[0] + " with payload: " + message[2]);
					receiveTracker++;
					summation += Integer.parseInt(message[2]);
				} else {
					//Message needs to continue on through the network
					for(int i = 0; i < path.length; i++) {
						if(path[i].equals(self)) {
							routeMessage(path[i+2], e);
							relayTracker++;
							break;
						}
					}
				}
			default:
				System.out.println("ERR:MessagingNode: invalid message type");
		
		}
	}
		
	public void connectToRegistry(String addr, Integer port) {		
		
		registry_socket = connectToNode(addr, port);
		
		if (registry_socket != null && registry_socket.isConnected()) { 
			//Node is connected to the server at this point			
			try {
				
				//Start a thread to listen to the server
				registry_listener = new TCPReceiverThread(registry_socket);
				registry_thread = new Thread(registry_listener);
				registry_thread.start();
				
				//Instantiate TCP sender object to send messages to the server
				registry_sender = new TCPSender(registry_socket);
				
				//Attempt to register with the server
				registry_sender.sendEvent(EventFactory.getInstance().createEvent(new String("0" + "\n" + registry_socket.getInetAddress().toString().substring(1) + "\n" + listening_port)));
				
				//Check for for a registration confirmation message
				Event message;
				while(true) {
					try {
						message = registry_listener.get();
						
						if(message != null) {
							System.out.println("Received message...");
							onEvent(message);
							return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	private Socket connectToNode(String addr, Integer port) {
		try {
			Socket result = new Socket(addr, port);
			return result;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void disconnectFromServer() {
		//Deregister with the server
		registry_sender.sendEvent(EventFactory.getInstance().createEvent(new String("2" + "\n" + registry_socket.getInetAddress().toString().substring(1) + "\n" + registry_socket.getLocalPort())));
		
		try {
			registry_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void routeMessage(String dest, Event message) {
		TCPSender sender;
		Socket socket;
		
		try {
			socket = connectToNode(dest.split(":")[0], Integer.parseInt(dest.split(":")[1]));
			sender = new TCPSender(socket);
			
			//Send the packet to continue it along the network
			sender.sendEvent(message);
			
			//Connection should be closed as quickly as possible
			socket.close();
		} catch (IOException e) {
			System.out.println("Could not connect to node");
		}
	}
	
	private void messageNodes(int num_rounds) {
		TCPSender sender;
		Socket socket;
		
		route_calculator = new ShortestPath(all_connections);
		
		for(int i = 0; i < num_rounds; i++) {
			try {
				NodeRepresentation sink = getRandomNode();
				route_calculator.calculateShortestPath(self, sink.toString());
				
				socket = connectToNode(route_calculator.getStartingNodeIP(), route_calculator.getStartingNodePort());
				sender = new TCPSender(socket);
				
				//Create new message that has the path the message the take and a random payload, and sends it to the starting node
				sender.sendEvent(EventFactory.getInstance().createEvent("6" + "\n" + route_calculator.getPath() + "\n" + ThreadLocalRandom.current().nextInt(-2147483648, 2147483647)));
				
				//Connection should be closed as quickly as possible
				socket.close();
				
				//TODO: probably need to make this update thread-safe
				sendTracker++;
			} catch (IOException e) {
				System.out.println("Could not connect to node");
			}
			
		}
	}

	private NodeRepresentation getRandomNode() {
		int random_index = ThreadLocalRandom.current().nextInt(0, all_nodes.size());
		
		//Get the index - implementation is due to the use of HashSet
		Iterator<NodeRepresentation> iter = all_nodes.iterator();
		for (int i = 0; i < random_index; i++) {
		    iter.next();
		}
		return iter.next();
	}
	
	private void start_listening(String registry_ip, int registry_port) {
		Event registry_message;
		
		//Start node listener thread
		node_listener = new TCPServerThread(6001);
		listening_port = node_listener.getPort();
		
		node_thread = new Thread(node_listener);
		node_thread.start();
		
		//Start input listener thread
		input_listener = new MessageNodeInputThread();
		Thread input_thread = new Thread(input_listener);
		input_thread.start();
		
		//Connect to registry and start registry threads
		connectToRegistry(registry_ip, registry_port);
		
		//Use the socket to determine IP address and assign name to this node
		self = registry_socket.getInetAddress().toString().substring(1) + ":" + listening_port;
		
		Integer new_input;
		Event new_event;
		while(true) {
			try {
				//Check if there are messages from the registry node
				registry_message = registry_listener.get();
				if(registry_message != null) {
					onEvent(registry_message);
				}
				
				//Check if another MessagingNode has connected and sent a message. 
				new_event = node_listener.getEvent();
				if(new_event != null) {
					System.out.println("MessagingNode::start_listening: new message received");
					//If there is, discard the socket and read the message
					node_listener.getSocket().close();
					onEvent(new_event);
				}
				
				//Check if there are any user inputs
				new_input = input_listener.get();
				if(new_input != null) {
					switch(new_input) {
						default:
							break;
					}
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public static void main(String[] args) {
		MessagingNode node = new MessagingNode();
		node.start_listening("127.0.0.1", 5001);
	}

}
