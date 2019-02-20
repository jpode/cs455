package cs455.overlay.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.MessagingNodeConnect;

public class NodeConnector {
	//Keeps track of how many nodes in the connected list have an active connection
	private ArrayList<NodeRepresentation> nodes_to_connect;
	//Runnable object to listen for connections from other nodes
	private TCPServerThread server;
	//Thread that the node_listener will run in
	private Thread server_thread;
	//Name of this node
	private String self;
	
	//These will be retrieved after finishing execution
	private ArrayList<NodeRepresentation> unconnected_nodes;
	private ArrayList<NodeRepresentation> connected_nodes;
	private ArrayList<Thread> thread_pool;
	private ArrayList<TCPReceiverThread> receiver_pool;
	
	public NodeConnector(String[] connections, ArrayList<NodeRepresentation> nodes_to_connect, String self, TCPServerThread server) {
		this.self = self; 
		this.server = server;
		this.nodes_to_connect = nodes_to_connect;
		unconnected_nodes = new ArrayList<NodeRepresentation>();
		connected_nodes = new ArrayList<NodeRepresentation>();
		
		receiver_pool = new ArrayList<TCPReceiverThread>();
		thread_pool = new ArrayList<Thread>();
		
		for(String address : connections) {
			unconnected_nodes.add(new NodeRepresentation(address.split(":")[0], Integer.parseInt(address.split(":")[1])));
		}

	}
	
	private synchronized void addConnection(NodeRepresentation node, TCPReceiverThread rec, Thread thd) {
		if(!connected_nodes.contains(node)){
						
			connected_nodes.add(node);
			unconnected_nodes.remove(node);

			receiver_pool.add(rec);
			thread_pool.add(thd);
			
			
		}
	}
	
	private synchronized void createConnection() {
		NodeRepresentation node = nodes_to_connect.get(0);
		
		if(!connected_nodes.contains(node)){
			Socket socket = connectToNode(node.toString().split(":")[0], Integer.parseInt(node.toString().split(":")[1]));
			if (socket != null && socket.isConnected()) { 
				node.addSocket(socket);
				connected_nodes.add(node);
				unconnected_nodes.remove(node);
				nodes_to_connect.remove(0);
				try {
					TCPSender sender = new TCPSender(node.getSocket());
					//Send a connection message
					sender.sendEvent(EventFactory.getInstance().createEvent("10" + "\n" + self.split(":")[0] + "\n" + self.split(":")[1]));
										
					TCPReceiverThread rec = new TCPReceiverThread(socket);
					Thread thd = new Thread(rec);
					thd.start();
					
					receiver_pool.add(rec);
					thread_pool.add(thd);
					
					
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	
	
	public void connectNodes() {
		//Start server thread to listen for connections
		server_thread = new Thread(server);
		server_thread.start();
		
		Socket new_connection;
		Event new_event;
		TCPReceiverThread new_receiver;
		Thread new_thread;
		NodeRepresentation new_node;
		
		while(!unconnected_nodes.isEmpty()) {
			
			//Check if there is a new connection from the ServerSocket
			new_connection = server.getSocket();
			if(new_connection != null) {
				//System.out.println("Registry::start_listening: new connection socket found");
				//If there is, get the associated event, active thread, and receiver
				new_receiver = server.getReciever();
				new_thread = server.getThread();
				new_event = server.getEvent();
				
				String[] message = ((MessagingNodeConnect)new_event).getSplitData();
				
				new_node = new NodeRepresentation(message[0], Integer.parseInt(message[1]), new_connection);
				addConnection(new_node, new_receiver, new_thread);
			}
			
			 //Try creating a new connection if there are still more nodes that should be connected to
			if (!nodes_to_connect.isEmpty()){
				createConnection();
			}
		}
		
	}
	
	public ArrayList<NodeRepresentation> getConnectedNodes(){
		return connected_nodes;
	}
	
	public ArrayList<TCPReceiverThread> getReceiverPool(){
		return receiver_pool;
	}
	
	public ArrayList<Thread> getThreadPool(){
		return thread_pool;
	}
	
}
