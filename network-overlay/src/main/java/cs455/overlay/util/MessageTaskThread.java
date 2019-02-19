package cs455.overlay.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.djikstra.ShortestPath;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.EventFactory;

public class MessageTaskThread implements Runnable {
	//Object to calculate the shortest path and provide the starting node
	private ShortestPath route_calculator;
	//List of all connections in the overlay with weights
	private Connection[] all_connections;
	//This is a HashSet because the total number of nodes in the overlay is not sent by the registry, and it avoids duplicates
	private HashSet<NodeRepresentation> all_nodes;
	//Name of this node
	private String self;
	private ConcurrentLinkedQueue<Integer> queue;
	private int num_rounds;
	private int sendTracker;

	public MessageTaskThread(int rounds, HashSet<NodeRepresentation> nodes, Connection[] connections, String node_name) {
		num_rounds = rounds;
		all_connections = connections;
		all_nodes = nodes;
		self = node_name;
	}

	//Non blocking call
	public Integer get() throws InterruptedException {
		return queue.poll();
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

	private NodeRepresentation getRandomNode() {
		int random_index = ThreadLocalRandom.current().nextInt(0, all_nodes.size());
		
		//Get the index - implementation is due to the use of HashSet
		Iterator<NodeRepresentation> iter = all_nodes.iterator();
		for (int i = 0; i < random_index; i++) {
		    iter.next();
		}
		return iter.next();
	}
	
	
	@Override
	public void run() {
		TCPSender sender;
		Socket socket;
		
		route_calculator = new ShortestPath(all_connections);
		
		for(int i = 0; i < num_rounds; i++) {
			try {
				NodeRepresentation sink = getRandomNode();
				route_calculator.calculateShortestPath(self, sink.toString());
				
				socket = connectToNode(route_calculator.getStartingNodeIP(), route_calculator.getStartingNodePort());
				sender = new TCPSender(socket);
				
				//Create new message that has the path the message the take and a random payload, and sends it to the starting node
				sender.sendEvent(EventFactory.getInstance().createEvent("6" + "\n" + route_calculator.getPath() + "\n" + ThreadLocalRandom.current().nextInt(-2147483648, 2147483647)));
				
				//Connection should be closed as quickly as possible
				socket.close();
				
				//TODO: probably need to make this update thread-safe
				sendTracker++;
			} catch (IOException e) {
				System.out.println("Could not connect to node");
			}
			
		}
		
		queue.add(sendTracker);
		while(!queue.isEmpty()) {
			; //Wait until the parent threads receives the statistics before exiting
		}
	}

}
