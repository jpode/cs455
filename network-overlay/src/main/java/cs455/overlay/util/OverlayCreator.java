package cs455.overlay.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.EventFactory;

public class OverlayCreator {
	private ArrayList<Connection> connection_list;
	int maxed_nodes; //Number of nodes that have the maximum number of connections

	public OverlayCreator() {
		connection_list = new ArrayList<Connection>();
	}
	
	public Connection[] constructOverlay(ArrayList<NodeRepresentation> node_registry, int num_connections) {
		
		/*Redundant loops here are due to the fact that each node needs to be aware of how many connections first.
		 *Additionally, the connections for each node may need to be reset if the overlay construction fails for some reason
		 *After the overlay is successfully constructed, each node needs to be send a message with its connections
		 */
		boolean overlay_constructed = false;
		
		while(!overlay_constructed) {
						
			for(NodeRepresentation node : node_registry) {
				node.setMaxConnections(num_connections);
			}
			
			//Generate connections for each node
			boolean construction_failure = false;
			maxed_nodes = 0;
			
			for(NodeRepresentation node : node_registry) {
				if(!generateConnections(node, node_registry, num_connections)) {
					//Overlay construction failed, try again by breaking the for loop
					construction_failure = true;
					break;
				}
			}
			
			if(!construction_failure) {
				overlay_constructed = true;
			} else {
				System.out.println("OverlayCreator: overlay construction failed, resetting and trying again...");
				connection_list.clear();
				for(NodeRepresentation node : node_registry) {
					node.resetConnections();
				}
			}
			
		}
		
		//Send nodes a message of their connections
		for(NodeRepresentation node : node_registry) {
			//Send out connection list to the current node to construct actual overlay connections

				TCPSender sender = new TCPSender(node.getSocket());
				String message_data = "3" + "\n" + node.getNumConnections() + "\n";
								
				for(int i = 0; i < node.getNumConnections(); i++) {
					message_data += node.getConnections()[i].toString() + "\n";
				}

				sender.sendEvent(EventFactory.getInstance().createEvent(message_data));		
		}
		
		return connection_list.toArray(new Connection[0]);
	}

	//Establish randomized connections for a given node and adds each connection to the connection list for the registry to keep track of
	//Node class ensures that a single node does not receive more than the indicated number of connections
	private boolean generateConnections(NodeRepresentation node, ArrayList<NodeRepresentation> node_registry, int num_connections) {
		int node_index = node_registry.indexOf(node);
		for(int i = node.getNumConnections() + 1; i <= num_connections; i++) {
			
			//If the number of maxed nodes is one less than the length of the registry, the node can't add any connections
			//If the node doesn't already have connections, it will be an island and the overlay will have to be constructed again
			if(maxed_nodes != node_registry.size() - 1) {
				int random_index = ThreadLocalRandom.current().nextInt(0, node_registry.size());
				int random_weight = ThreadLocalRandom.current().nextInt(1, 11);
	
	
				if(random_index != node_index) {
					NodeRepresentation random_node = node_registry.get(random_index);
					if(random_node.establishConnection(node)){
						node.establishConnection(random_node);
						connection_list.add(new Connection(node, random_node, random_weight));
						
						 //Checks if either/both nodes have maxed out the number of connections that can be held, helping identify islands
						if(node.isMaxed()) {
							maxed_nodes++;
						}
						
						if(random_node.isMaxed()) {
							maxed_nodes++;
						}

					} else { //Randomly selected node already has the max number of connections or the connection has already been made, a new connection is tried 
						i--; 
					}
				} else { //Randomly selected node is trying to connection to itself,  a new connection is tried 
					i--;
				}
			} else if(node.getNumConnections() == 0){ //Node is an island
				return false;
			} else { //Node will not max number of connections, but the overlay will still work
				return true;
			}
		}
		return true;
	}
}
