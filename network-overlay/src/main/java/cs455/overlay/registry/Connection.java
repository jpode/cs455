package cs455.overlay.registry;

public class Connection {
	private Node node_1;
	private Node node_2;
	private int weight;
	
	public Connection(Node n1, Node n2, int w) {
		node_1 = n1;
		node_2 = n2;
		weight = w;
	}
	
	public Node getFirstNode() {
		return node_1;
	}
	
	public Node getSecondNode() {
		return node_2;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public String toString() {
		return node_1.toString() + " " + node_2.toString() + " " + Integer.toString(weight);
	}
}
