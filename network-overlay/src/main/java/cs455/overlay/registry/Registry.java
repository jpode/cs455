package cs455.overlay.registry;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.wireformats.RegisterResponse;;

/* Registry class provides four functions:
 * 
 * Register nodes
 * De-register nodes
 * Construct overlay connections between nodes
 * Assign link weights
 * 
 */
public class Registry {
	//Static instance variable to follow singleton pattern
	private static Registry instance;
	//Number of connections that each message node will have in the overlay.
	Integer NUM_CONNECTIONS;
	//Registry data structure. MessageNodes contain a list of connections
	ArrayList<Node> node_registry;
	ArrayList<Connection> connection_list;
	
	//Private constructor to follow singleton pattern and ensure that only one registry class exists
	private Registry() {
		node_registry = new ArrayList<Node>();
		connection_list = new ArrayList<Connection>();
	}
	
	//Threadsafe getter method
	public static synchronized Registry getInstance() {
		if(instance == null) {
			instance = new Registry();
		}
		return instance;
	}
	
	public byte[] handlePacket(byte[] packet, Socket cs) {
		String message = new String(packet);
		String[] lines = message.split("\n");
		
		int message_type = Integer.parseInt(lines[0]);
		
		switch(message_type) {
			case(1):
				Node node = new Node(lines[1], Integer.parseInt(lines[2]), cs);
				if(register(node)) {
					return new RegisterResponse((byte)0, "Registration successful").getPacket();
				}
				return new RegisterResponse((byte)1, "Registration unsuccessful").getPacket();
			case(7):
				return null;
			case(9):
				return null;
			default:
				System.out.println("ERROR: Server should not receive a type " + message_type + " packet");
				return null;
			
		}
	}
	
	//Request to register node. Fails if node is already registered, otherwise registers
	public boolean register(Node node) {
		for(Node check_node : node_registry) {
			if(node.equals(check_node)) {
				return false;
			}
		}
		
		node_registry.add(node);
		return true;
	}
	
	//Request to deregister node. Fails if node is not registered, otherwise deregisters
	public int deregister(Node node) {
		for(Node check_node : node_registry) {
			if(node.equals(check_node)) {
				node_registry.remove(check_node);
				return 0;
			}
		}
		
		return 1;
	}
	
	public int getNumConnections() {
		return NUM_CONNECTIONS;
	}

	public String listMessageNodes() {
		String output = "";
		
		for(Node node : node_registry) {
			output += node.toString() + "\n";
		}
		
		return output;
	}

	public String listWeights() {
		String output = "";
		
		for(Connection conn : connection_list) {
			output += conn.toString() + "\n";
		}
		
		return output;
		
	}

	public void sendOverlayLinkWeights() {
		// TODO Auto-generated method stub
		
	}

	public void start(String num_rounds) {
		// TODO Auto-generated method stub
		
	}

	public void constructOverlay(String num_connections) {
		NUM_CONNECTIONS = Integer.parseInt(num_connections);
		
		//Redundancy here is due to the fact that each node needs to be aware of how many connections can be made
		//TODO: more elegant solution. Possibly control number of connections made from Registry class insteadof Node
		for(Node node : node_registry) {
			node.setMaxConnections(NUM_CONNECTIONS);
		}
		
		for(Node node : node_registry) {
			generateConnections(node);
		}
		
	}
	
	//Establish randomized connections for a given node
	//Node class ensures that a single node does not receive more than the indicated number of connections
	private void generateConnections(Node node) {
		int node_index = node_registry.indexOf(node);
				
		for(int i = node.getNumConnections() + 1; i <= NUM_CONNECTIONS; i++) {
			int random_index = ThreadLocalRandom.current().nextInt(0, node_registry.size());
			int random_weight = ThreadLocalRandom.current().nextInt(1, 11);

			//Ensure a node does not connect to itself
			if(random_index != node_index) {
				Node random_node = node_registry.get(random_index);
				if(random_node.establishConnection(node)){
					node.establishConnection(random_node);
					connection_list.add(new Connection(node, random_node, random_weight));
					System.out.println("Connection " + i + " established for node " + node.toString());
				} else { //The randomly selected node already has the max number of connections, a new connection is tried 
					i--; 
				}
			}
		}
	}
	 
}
