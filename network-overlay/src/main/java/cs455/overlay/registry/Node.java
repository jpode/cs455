package cs455.overlay.registry;

import java.io.IOException;
import java.net.Socket;


//The Node class is a representation of a client node for use by the registry

public class Node {
	private String ip_addr;
	private Integer port;
	private Integer MAX_CONNECTIONS;
	private Socket self_socket;
	private Node[] connections;
	
	
	public Node(String ip, Integer p, Integer n, Socket socket) {
		ip_addr = ip;
		port = p;
		MAX_CONNECTIONS = n;
		self_socket = socket;
		connections = new Node[MAX_CONNECTIONS];
	}

	//Indicates a bidirectional connection between this node and the parameter node. 
	//The Node class does not handle creating the other part of the bidirectional connection, that should be done by the registry. 
	//Returns true if there is room in the connections array, otherwise returns false
	public boolean establishConnection(Node node) {
		for(int i = 0; i < connections.length; i++) {
			if(connections[i] != null) {
				connections[i] = node;
				return true;
			}
		}
		return false;
	}
	
	public Node[] getConnections() {
		return connections;
	}
	
	//Remove a node from this node's connection list
	public boolean disconnectNode(Node node) {
		for(int i = 0; i < connections.length; i++) {
			if(connections[i].equals(node)) {
				connections[i] = null;
				return true;
			}
		}
		return false;
	}
	
	public Socket getSocket() {
		return self_socket;
	}
	
	//Kill this node's socket and remove connections
	public void kill() {
		
		try {
			self_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < connections.length; i++) {
			connections[i] = null;
		}
	}

	//Generate equals method
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		if (ip_addr == null) {
			if (other.ip_addr != null)
				return false;
		} else if (!ip_addr.equals(other.ip_addr))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}
}
