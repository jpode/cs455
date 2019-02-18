package cs455.overlay.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
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
import cs455.overlay.util.Connection;
import cs455.overlay.util.NodeRepresentation;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.Message;

public class MessagingNode implements Node{
	
	private Socket socket;
	private DataInputStream input_stream;
	private DataOutputStream output_stream;
	private TCPReceiverThread server_listener;
	private TCPSender server_sender;
	private int listening_port;
	private Thread server_thread;
	private String[] connections;
	private Connection[] all_connections;
	private HashSet<NodeRepresentation> all_nodes; //This is a HashSet because the total number of nodes in the overlay is not sent by the registry, and avoids duplicates
	private ShortestPath route_calculator;
	private int sendTracker;
	private int receiveTracker;
	
	public MessagingNode() {
		all_nodes = new HashSet<NodeRepresentation>();
		sendTracker = 0;
		receiveTracker = 0;
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
					server_listener.kill();
					server_thread.interrupt();
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
			default:
				System.out.println("ERR:MessagingNode: invalid message type");
		
		}
	}
		
	public void connectToRegistry(String addr, Integer port) {		
		
		if (socket != null && socket.isConnected()) { 
			//Node is connected to the server at this point			
			try {
				Socket socket = new Socket(addr, port);
				
				//Start a thread to listen to the server
				server_listener = new TCPReceiverThread(socket);
				server_thread = new Thread(server_listener);
				server_thread.start();
				
				//Instantiate TCP sender object to send messages to the server
				server_sender = new TCPSender(socket);
				
				//Attempt to register with the server
				server_sender.sendEvent(EventFactory.getInstance().createEvent(new String("0" + "\n" + socket.getInetAddress().toString().substring(1) + "\n" + listening_port)));
				
				//Check for for a registration confirmation message
				Event message;
				while(true) {
					try {
						message = server_listener.get();
						
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
		server_sender.sendEvent(EventFactory.getInstance().createEvent(new String("2" + "\n" + socket.getInetAddress().toString().substring(1) + "\n" + socket.getLocalPort())));
		
		// Close streams and then sockets
		try {
			input_stream.close();
			output_stream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//TODO: possibly thread this
	private void messageNodes(int num_rounds) {
		TCPSender sender;
		Socket socket;
		for(int i = 0; i < num_rounds; i++) {
			try {
				NodeRepresentation sink = getRandomNode();
				route_calculator.calculateRoute(sink);
				
				socket = connectToNode(route_calculator.getStartingNodeIP(), route_calculator.getStartingNodePort());
				sender = new TCPSender(socket);
				
				//Create new message that has the path the message the take and a random payload, and sends it to the starting node
				sender.sendEvent(EventFactory.getInstance().createEvent("6" + "\n" + "\n" + route_calculator.getPath() + "\n" + ThreadLocalRandom.current().nextInt(-2147483648, 2147483647)));
				
				socket.close();
				//TODO: probably need to make this update thread-safe
				sendTracker++;
			} catch (IOException e) {
				e.printStackTrace();
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
	
	private void start_listening() {
		Event registry_message;
		
		//Start threads
		TCPServerThread server = new TCPServerThread(6001);
		listening_port = server.getPort();
		
		Thread server_thread = new Thread(server);
		server_thread.start();
		
		//Connect to registry
		connectToRegistry("127.0.0.1", 5001);

		
		while(true) {
			try {
				registry_message = server_listener.get();
				if(registry_message != null) {
					onEvent(registry_message);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public static void main(String[] args) {
		MessagingNode node = new MessagingNode();
		node.start_listening();
	}

}
