package cs455.overlay.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.EventFactory;

public class OverlayCreator {
	 private ArrayList<Connection> connection_list;
	 
	public OverlayCreator() {
		connection_list = new ArrayList<Connection>();
	}
	
	public Connection[] constructOverlay(ArrayList<NodeRepresentation> node_registry, int num_connections) {
		
		//Redundant loops here are due to the fact that each node needs to be aware of how many connections can be made
		//Another data structure would be needed to keep track of the number of connections made if this was not the case,
		// so it is easier to make the node keep track of it. It can't be included in the outer loop, because 
		// later nodes may end up getting too many connections before they know how many they can have
		for(NodeRepresentation node : node_registry) {
			node.setMaxConnections(num_connections);
		}
		
		for(NodeRepresentation node : node_registry) {
			
			//Send out connection list to the current node to construct actual overlay connections
			try {
				TCPSender sender = new TCPSender(node.getSocket());
				String message_data = "3" + "\n" + node.getNumConnections() + "\n";
				
				for(NodeRepresentation connected_node : node.getConnections()) {
					message_data += connected_node.toString() + "\n";
				}
				
				sender.sendEvent(EventFactory.getInstance().createEvent(message_data));
				
				//Once the connection list is sent, create connection objects for the server to keep track of
				generateConnections(node, node_registry, num_connections); 
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return (Connection[]) connection_list.toArray();
	}
	
	//Establish randomized connections for a given node
	//Node class ensures that a single node does not receive more than the indicated number of connections
	private void generateConnections(NodeRepresentation node, ArrayList<NodeRepresentation> node_registry, int num_connections) {
		int node_index = node_registry.indexOf(node);
				
		for(int i = node.getNumConnections() + 1; i <= num_connections; i++) {
			int random_index = ThreadLocalRandom.current().nextInt(0, node_registry.size());
			int random_weight = ThreadLocalRandom.current().nextInt(1, 11);

			//Ensure a node does not connect to itself
			if(random_index != node_index) {
				NodeRepresentation random_node = node_registry.get(random_index);
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
