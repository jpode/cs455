package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.djikstra.RoutingCache;
import cs455.overlay.djikstra.ShortestPath;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.ServerListenerThread;
import cs455.overlay.util.Connection;
import cs455.overlay.util.MessageNodeInputThread;
import cs455.overlay.util.MessageTaskThread;
import cs455.overlay.util.NodeConnector;
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
	//Object to listen for other nodes
	TCPServerThread server;
	//Port that this node is listening on for connections from other nodes - this will be sent to the registry
	private int listening_port;
	//Thread to listen for user inputs
	private MessageNodeInputThread input_listener;
	//Runnable to complete a messaging task
	private MessageTaskThread task;
	//Thread for task to run on
	private Thread task_thread;
	//Runnable object to listen for messages from other connected nodes
	private ServerListenerThread server_listener;
	//Thread to run the server_listener on
	private Thread listener_thread;
	//Pools for server listener to use
	private ArrayList<TCPReceiverThread> receiver_pool;
	private ArrayList<Thread> rec_thread_pool;
	//List of nodes that this node directly connects to. Format: <ip>:<port>
	private String[] connections;
	//Pool of active connections
	private ArrayList<NodeRepresentation> active_connections;
	//List of all connections in the overlay with weights
	private Connection[] all_connections;
	//Generates connections for all nodes this node is linked to
	private NodeConnector node_connector;
	//Cache for all currently discovered routes
	private RoutingCache cache;
	//Used to display shortest routes after the cache is reset
	private String cached_routes;
	//This is a HashSet because the total number of nodes in the overlay is not sent by the registry, and it avoids duplicates
	private HashSet<NodeRepresentation> all_nodes; 
	//Returns true if there is currently a thread sending messages to other nodes
	private boolean running_task;
	//Returns true if all nodes are connected
	private boolean nodes_connected;
	//Metric tracking values
	private int sendTracker;
	private int receiveTracker;
	private int relayTracker;
	private long sendSummation;
	private long receiveSummation;
	
	public MessagingNode() {
		cache = new RoutingCache();
		all_nodes = new HashSet<NodeRepresentation>();
		running_task = false;
		nodes_connected = false;
		sendTracker = 0;
		receiveTracker = 0;
		relayTracker = 0;
		sendSummation = 0;
		receiveSummation = 0;
	}
	
	public void onEvent(Event e) {
		String[] message;
		switch(e.getType()) {
			case(1): //RegisterResponse
				RegisterResponse response = (RegisterResponse)e;
				if(response.getStatusCode() == 0) { //Registration successful
					System.out.println("MessagingNode::connectToServer: registration successful");
					message = ((RegisterResponse)e).getSplitData();
					break;
				} else if(response.getStatusCode() == 1){ //Registration unsuccessful
					System.out.println("ERR:MessagingNode::connectToServer: registration not successful");
					registry_listener.kill();
					registry_thread.interrupt();
					break;
				} else if(response.getStatusCode() == 2){ //Deregistration successful
					System.out.println("Deregistration successful");
					try {
						reset();
						registry_socket.close();
						registry_listener.killAll();
						registry_thread.interrupt();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					break;
				} 
				//Deregistration unsuccessful - do nothing
				System.out.println("Could not exit overlay");
					
				
				break;
			case(3)://MessagingNodesList
				message = ((MessagingNodesList)e).getSplitData();
				connections = new String[Integer.parseInt(message[1])];
				for(int i = 2; i < message.length; i++) {
					connections[i-2] = message[i];
				}

				
				break;
			case(4)://LinkWeights
				//Indicates to the NodeConnector which nodes this node should send a connection to. The other connections will connect to it
				ArrayList<NodeRepresentation> nodes_to_connect = new ArrayList<NodeRepresentation>();
			
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
					
					if(node_1.toString().equals(self)) {
						nodes_to_connect.add(node_2);
					}
				}
				
				
				node_connector = new NodeConnector(connections, nodes_to_connect, self, server);
				node_connector.connectNodes();
				nodes_connected = true;
				
				
				active_connections = node_connector.getConnectedNodes();
				receiver_pool = node_connector.getReceiverPool();
				rec_thread_pool = node_connector.getThreadPool();
				
				server_listener = new ServerListenerThread();

				for(int i = 0; i < receiver_pool.size(); i++) {
					server_listener.addConnection(receiver_pool.get(i), rec_thread_pool.get(i));
				}
				
				//Start server listener thread to listen for messages from node connections
				listener_thread = new Thread(server_listener);
				listener_thread.start();
				
				break;
			case(5)://TaskInitiate
				message = ((TaskInitiate)e).getSplitData();
				task = new MessageTaskThread(Integer.parseInt(message[1]), active_connections, all_nodes, all_connections, self, cache);
				
				task_thread = new Thread(task);
				task_thread.start();

				running_task = true;
				break;
			case(6)://Message
				message = ((Message)e).getSplitData();
				String[] path = message[1].split("--");
				if(path[path.length-1].equals(self)) {
					//Message has reached it's destination
					//System.out.println("Node received message from " + path[0] + " with payload: " + message[2]);
					receiveTracker++;
					receiveSummation += Integer.parseInt(message[2]);
				} else {
					//Message needs to continue on through the network
					for(int i = 0; i < path.length; i++) {
						if(path[i].equals(self)) {
							//System.out.println("Relaying message to " + path[i+2]);
							routeMessage(path[i+2], e);
							relayTracker++;
							break;
						}
					}
				}
				
				break;
			case(8)://PullTrafficSummary
				System.out.println("Node Statistics:");
				System.out.println("\tNumber of messages sent:" + sendTracker);
				System.out.println("\tNumber of messages received:" + receiveTracker);
				System.out.println("\tSum of messages sent:" + sendSummation);
				System.out.println("\tSum of messages received:" + receiveSummation);
				System.out.println("\tNumber of messages relayed:" + relayTracker);
				System.out.println();
				System.out.println("Sending statistics to server..");
				
				registry_sender.sendEvent(EventFactory.getInstance().createEvent(new String("9" + "\n" + registry_socket.getLocalAddress().toString().substring(1) 
						+ "\n" + listening_port+ "\n" + sendTracker + "\n" + sendSummation + "\n" + receiveTracker + "\n" + receiveSummation + "\n" + relayTracker)));
			
				System.out.println("Sent all statistics");
				
				//Reset to prepare for next overlay/messaging send cycle
				cached_routes = cache.getAllRoutes();
				reset();
				break;
			case(11):
				System.out.println("Received test message");
				break;
			default:
				//System.out.println("ERR:MessagingNode: invalid message type");		
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
				registry_sender.sendEvent(EventFactory.getInstance().createEvent(new String("0" + "\n" + registry_socket.getLocalAddress().toString().substring(1) + "\n" + listening_port)));
				
				//Check for for a registration confirmation message
				Event message;
				while(true) {
					try {
						message = registry_listener.get();
						
						if(message != null) {
							//System.out.println("Received message...");
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
	
	public void exitOverlay() {
		//Deregister with the registry - must receive a response before doing anything else
		registry_sender.sendEvent(EventFactory.getInstance().createEvent(new String("2" + "\n" + self.split(":")[0] + "\n" + self.split(":")[1])));

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
	
	private void routeMessage(String dest, Event message) {
		//Node to send the packet to
		NodeRepresentation node = new NodeRepresentation(dest.split(":")[0], Integer.parseInt(dest.split(":")[1]));
		//Use the MessageTaskThread object to send the message
		task.sendMessage(node, message);
	}

	//Finish task by sending TaskComplete message to registry 
	private void finishTask() {
		System.out.println("Finished task, sent " + sendTracker + " messages");
		registry_sender.sendEvent(EventFactory.getInstance().createEvent(new String("7" + "\n" + registry_socket.getLocalAddress().toString().substring(1) + "\n" + listening_port)));
	}
	
	//Reset back to after registering
	private void reset() {
		System.out.println("Resetting node...");
		
		if(task_thread != null) {
			task_thread.interrupt();
		}
		
		active_connections = new ArrayList<NodeRepresentation>();
		receiver_pool = new ArrayList<TCPReceiverThread>();
		rec_thread_pool = new ArrayList<Thread>();
		connections = null;
		all_connections = null;
		cache = new RoutingCache();
		all_nodes = new HashSet<NodeRepresentation>();
		server_listener = null;
		nodes_connected = false;
		running_task = false;
		sendTracker = 0;
		receiveTracker = 0;
		relayTracker = 0;
		sendSummation = 0;
		receiveSummation = 0;
		
		System.out.println("Node reset, waiting for info from registry");
	}
	
	private void start_listening(String registry_ip, int registry_port) {		
		server = new TCPServerThread(6001);
		listening_port = server.getPort();
		
		//Start input listener thread
		input_listener = new MessageNodeInputThread();
		Thread input_thread = new Thread(input_listener);
		input_thread.start();
		
		//Connect to registry and start registry threads
		connectToRegistry(registry_ip, registry_port);
		
		//Use the socket to determine IP address and use the server listening port to assign name to this node
		self = registry_socket.getLocalAddress().toString().substring(1) + ":" + listening_port;
		
		Integer new_input;
		Long task_result;
		Event new_event;
		
		while(registry_listener.isListening()) { //Listen while connected to the registry, otherwise terminate
			try {
				//Check if there are messages from the registry node
				new_event = registry_listener.get();
				if(new_event != null) {
					onEvent(new_event);
				}
				
				//Check if there are any messages from MessagingNode connections
				if(nodes_connected) {
					new_event = server_listener.get();
					if(new_event != null) {
						onEvent(new_event);
					}
				}
				
				//Check if there are any user inputs
				new_input = input_listener.get();
				if(new_input != null) {
					switch(new_input) {
						case(0)://print-shortest-path
							if(running_task) {
								System.out.println(cache.getAllRoutes());
							} else {
								System.out.println(cached_routes);
							}
							break;
						case(1)://exit-overlay
							exitOverlay();
							break;
						case(2)://send test message
							messageAllNodes(EventFactory.getInstance().createEvent("11"));
						default:
							break;
					}
				}
				
				//If there is a task running, check to see if it has returned a value and therefore is finished
				if(running_task) {					
					task_result = task.getStatistics();
					if(task_result != null) {
						//System.out.println("MessagingNode task finished");
						//The thread will end on its own
					
						sendTracker = task_result.intValue();
						//Retrieve the summation value
						sendSummation = task.getStatistics();
						finishTask();
						running_task = false;
					}
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	private void messageAllNodes(Event e) {
		for(NodeRepresentation node : active_connections) {
				TCPSender sender = new TCPSender(node.getSocket());
				sender.sendEvent(e);
		}
	}

	public static void main(String[] args) {
		if(args.length >= 2) {
			MessagingNode node = new MessagingNode();
			System.out.println("Connecting to server at " + args[0] + ":" + args[1]);

			node.start_listening(args[0], Integer.parseInt(args[1]));

			System.out.println("Node no longer connected to server, terminating.");
			System.exit(1);
		} else {
			System.out.println("MessagingNode startup failure - not enough arguments");
		}

	}

}
