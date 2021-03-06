package cs455.overlay.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


//The Node class is a representation of a client node for use by the registry

public class NodeRepresentation {
	private String ip_addr;
	private Integer port;
	private Socket self_socket;
	private DataInputStream din;
	private NodeRepresentation[] connected_nodes;
	private boolean overlay_constructed; //True if the overlay is being/has been constructed
	private int connection_counter;
	
	public NodeRepresentation(String ip, Integer p, Socket socket) {
		ip_addr = ip;
		port = p;
		self_socket = socket;
		overlay_constructed = false;
		connection_counter = 0;
	}
	
	public NodeRepresentation(String ip, Integer p) {
		ip_addr = ip;
		port = p;
		self_socket = null;
		overlay_constructed = false;
		connection_counter = 0;
	}

	public void setMaxConnections(int num_connections) {
		connected_nodes = new NodeRepresentation[num_connections];
		overlay_constructed = true;
	}
	
	//Indicates a bidirectional connection between this node and the parameter node. 
	//The Node class does not handle creating the other part of the bidirectional connection, that should be done by the registry. 
	//Returns true if there is room in the connections array, otherwise returns false
	public boolean establishConnection(NodeRepresentation node) {
		if(overlay_constructed && connection_counter < connected_nodes.length) {
			
			//First check if a connection was already made with this node
			for(int i = 0; i < connected_nodes.length; i++) {
				 if(!(connected_nodes[i] == null) && connected_nodes[i].equals(node)) { //Connection already exists
					 //System.out.println((ip_addr + port) + " cannot establish connection with " + node.toString() + " because the connection already exists");
						return false;
					}  
			}

			//If there is available connection space, find it and add the new connection
			for(int i = 0; i < connected_nodes.length; i++) {
				if(connected_nodes[i] == null) {	
					connected_nodes[i] = node;
					connection_counter++;
					return true;
				} 
			}
		}
		return false;
	}
	
	public NodeRepresentation[] getConnections() {
		return connected_nodes;
	}
	
	public int getNumConnections() {
		return connection_counter;
	}
	
	//Returns true if this node has the max number of connections
	public boolean isMaxed() {
		return connection_counter >= connected_nodes.length; 
	}
	
	//Remove a node from this node's connection list
	public boolean disconnectNode(NodeRepresentation node) {
		if(overlay_constructed) {
			for(int i = 0; i < connected_nodes.length; i++) {
				if(connected_nodes[i].equals(node)) {
					connected_nodes[i] = null;
					return true;
				}
			}
		}
		return false;
	}
	
	public Socket getSocket() {
		return self_socket;
	}
	
	public DataInputStream getDataInputStream() {
		return din;
	}
	
	//Kill this node's socket and remove connections
	public void kill() {
		
		try {
			if(din != null) {
				din.close();
			}
			self_socket.close();
		} catch (IOException e) {
			//ignore
		}
		resetConnections();
	}

	//Reset the connections for this node
	public void resetConnections() {
		if(overlay_constructed) {
			for(int i = 0; i < connected_nodes.length; i++) {
				connected_nodes[i] = null;
			}
			connection_counter = 0;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip_addr == null) ? 0 : ip_addr.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeRepresentation other = (NodeRepresentation) obj;
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
	
	public String toString() {
		return ip_addr + ":" + port;
	}

	public void addSocket(Socket socket) {
		self_socket = socket;
	}
}
