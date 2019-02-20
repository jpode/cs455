package cs455.overlay.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.djikstra.RoutingCache;
import cs455.overlay.djikstra.ShortestPath;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.Message;

public class MessageTaskThread implements Runnable {
	//Object to calculate the shortest path and provide the starting node
	private ShortestPath route_calculator;
	//Routing cache, used by route_calculator
	private RoutingCache cache;
	//List of all connections in the overlay with weights
	private Connection[] all_connections;
	//This is a HashSet because the total number of nodes in the overlay is not sent by the registry, and it avoids duplicates
	private HashSet<NodeRepresentation> all_nodes;
	//Pool of active connections
	private ArrayList<NodeRepresentation> conn_pool;
	//Name of this node
	private String self;
	//Queue for after messages have been sent
	private ConcurrentLinkedQueue<Long> statistics_queue;
	//Queue for connections created by the task thread, which the main MessagingNode class with listen for events on
	//TCPSender object
	private TCPSender sender;
	
	private int num_rounds;
	private long sendTracker;
	private long sendSummation;
	private boolean done;

	//Task thread sends messages either by creating them, or by routing them when instructed to do so by the MessagingNode class
	//Does not listen for messages from connections
	public MessageTaskThread(int num_rounds, ArrayList<NodeRepresentation> connected_nodes, HashSet<NodeRepresentation> all_nodes, Connection[] all_connections, String node_name, RoutingCache cache) {
		this.num_rounds = num_rounds;
		this.all_connections = all_connections;
		this.all_nodes = all_nodes;
		this.conn_pool = connected_nodes;
		self = node_name;
		this.cache = cache;
		
		statistics_queue = new ConcurrentLinkedQueue<Long>();

		sendTracker = 0;
		sendSummation = 0;
		
		done = false;
	}
	
	//Non blocking call
	public Long getStatistics() throws InterruptedException {
		if(done) {
			return statistics_queue.poll();
		} 
		return null;
	}
	
	//Selects a random node to be sent a message based on the number of nodes in the overlay
	private NodeRepresentation getRandomNode() {
		NodeRepresentation random_node;
		int random_index;
		
		while(true) {
			random_index = ThreadLocalRandom.current().nextInt(0, all_nodes.size());
			//Get the node at the index - implementation is due to the use of HashSet
			Iterator<NodeRepresentation> iter = all_nodes.iterator();
			
			for (int i = 0; i < random_index; i++) {
			    iter.next();
			}
			
			random_node = iter.next();
			
			if(!random_node.toString().equals(self)) { //If the selected node is itself, retry selection. If not, return
				return random_node;
			}
		}
	}
	
	//Sends messages to other nodes by checking for an existing connection or creating on if it does not exist
	public synchronized void sendMessage(NodeRepresentation node, Event message) {
		
		sender = new TCPSender(conn_pool.get(conn_pool.indexOf(node)).getSocket());
		sender.sendEvent(message);
	}
	
	//Run method creates new messages and passes them off to the sendMessage method, and when finished creating messages it will push the statistics variables into a queue for retrieval
	@Override
	public void run() {
		int payload;

		route_calculator = new ShortestPath(all_connections, cache);
		NodeRepresentation node;
		String debug_sink;
		for(int i = 0; i < num_rounds; i++) {
			//This is the sink
			node = getRandomNode();
			debug_sink = node.toString();
			route_calculator.calculateShortestPath(self, node.toString());
			
			//This is the starting node
			node = new NodeRepresentation(route_calculator.getStartingNodeIP(), route_calculator.getStartingNodePort());
			
			//Create random payload
			payload = ThreadLocalRandom.current().nextInt(-2147483648, 2147483647);
			
			//Create new message that has the path the message the take and a random payload, and sends it to the starting node				
			sendMessage(node, EventFactory.getInstance().createEvent("6" + "\n" + route_calculator.getPath() + "\n" + payload));
							
			sendTracker++;
			sendSummation += payload;
			//System.out.println("Successfully sent message " + (i+1) + " of " + num_rounds + " to " + debug_sink);
		}
		
		statistics_queue.add(sendTracker);
		statistics_queue.add(sendSummation);
		
		done = true;
		
		//System.out.println("TaskThread completed task, waiting for parent thread to interrupt");
		while(true) {
			; //Because the task thread is responsible for routing messages, it does not end until interrupted
		}

	}

}
