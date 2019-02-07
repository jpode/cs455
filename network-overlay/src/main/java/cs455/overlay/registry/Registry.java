package cs455.overlay.registry;

import java.util.ArrayList;;

/* Registry class provides four functions:
 * 
 * Register nodes
 * De-register nodes
 * Construct overlay connections between nodes
 * Assign link weights
 * 
 */
public class Registry {
	//Number of connections that each message node will have in the overlay. Default is 4.
	Integer NUM_CONNECTIONS = 4;
	//Registry data structure. MessageNodes contain a list of connections
	ArrayList<Node> node_registry;
	
	public Registry() {
		node_registry = new ArrayList<Node>();
	}
	
	public Registry(Integer n) {
		node_registry = new ArrayList<Node>();
	}
	
	//Request to register node. Fails if node is already registered, otherwise registers
	public int register(Node node) {
		for(Node check_node : node_registry) {
			if(node.equals(check_node)) {
				return 1;
			}
		}
		
		node_registry.add(node);
		return 0;
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

	public void listMessageNodes() {
		// TODO Auto-generated method stub
		
	}

	public void listWeights() {
		// TODO Auto-generated method stub
		
	}

	public void sendOverlayLinkWeights() {
		// TODO Auto-generated method stub
		
	}

	public void start(String substring) {
		// TODO Auto-generated method stub
		
	}
	 
}
