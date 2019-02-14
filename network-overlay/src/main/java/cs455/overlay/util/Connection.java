package cs455.overlay.util;

//Connection class represents a connection between two messaging nodes, and has a randomly assigned weight
//This does not guarantee that the nodes made this connection, but it means the server told the nodes to make this connection
// and is under the assumption that this connection exists as long as the connection object exists.
//Each connection between two nodes is unique - only one connection object exists per connection, though it is bidirectional
public class Connection {
	private NodeRepresentation node_1;
	private NodeRepresentation node_2;
	private int weight;
	
	public Connection(NodeRepresentation n1, NodeRepresentation n2, int w) {
		node_1 = n1;
		node_2 = n2;
		weight = w;
	}
	
	public NodeRepresentation getFirstNode() {
		return node_1;
	}
	
	public NodeRepresentation getSecondNode() {
		return node_2;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public String toString() {
		return node_1.toString() + " " + node_2.toString() + " " + Integer.toString(weight);
	}
}
