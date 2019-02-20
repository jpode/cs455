package cs455.overlay.djikstra;

import java.util.ArrayList;

import cs455.overlay.util.Connection;

public class ShortestPath {

	private Connection[] connection_list;
	private ArrayList<String> all_nodes;
	private String path;
	private RoutingCache cache;
	
	public ShortestPath(Connection[] connections) {
		connection_list = connections;
		all_nodes = new ArrayList<String>();
		
		for(Connection conn : connection_list) {
			if(!all_nodes.contains(conn.getFirstNode().toString())){
				all_nodes.add(conn.getFirstNode().toString());
			}
			
			if(!all_nodes.contains(conn.getSecondNode().toString())){
				all_nodes.add(conn.getSecondNode().toString());
			}
		}
		
		cache = new RoutingCache();
	}
	
	public String getStartingNodeIP() {
		return path.split("--")[2].split(":")[0];
	}
	
	public int getStartingNodePort() {
		return Integer.parseInt(path.split("--")[2].split(":")[1]);
	}
	
	public String getPath() {
		return path;
	}
	
	public void calculateShortestPath(String source, String sink) {

		path = cache.checkForPath(source, sink);
		if(path == null) {
			ArrayList<DjikstraNode> nodes = new ArrayList<DjikstraNode>();
			
			int current_index = -1;

			for(int i = 0; i < all_nodes.size(); i++) {
				nodes.add(new DjikstraNode(all_nodes.get(i).toString()));
				nodes.get(i).calculateAdjacencies(connection_list);
				
				//Set distance of source node to 0
				if(nodes.get(i).toString().equals(source)) {
					nodes.get(i).setDist(0);
					current_index = i;
				}
			}

			DjikstraNode current_node = nodes.get(current_index);

			while(!nodes.isEmpty()) {
				nodes.remove(current_node);
				
				for(DjikstraNode adj_node : current_node.getAdjacencies()) {
					if(nodes.contains(adj_node) && current_node.getDist() + adj_node.getDist() < nodes.get(nodes.indexOf(adj_node)).getDist()) {						
						nodes.get(nodes.indexOf(adj_node)).setDist(current_node.getDist() + adj_node.getDist());
						nodes.get(nodes.indexOf(adj_node)).setPrev(current_node, adj_node.getDist());
					}
				}

				current_node = getLeastDistanceNode(nodes);

				//Current node is the sink, at which point the algorithm is done
				if(current_node.toString().equals(sink)){
					path = iteratePath(current_node);
					cache.addToCache(path);
					return;
				}
			}
		} else {
			//Path was cached
		}
		//Path was not cached and sink node was not reached, path is null
	}
	
	private String iteratePath(DjikstraNode current_node) {
		DjikstraNode previous = current_node.getPrev();
		String path = current_node.getPrevDist() + "--" + current_node.getNode();

		while(previous != null) {
			path = previous.getPrevDist() + "--" + previous.getNode() + "--" + path;
			previous = previous.getPrev();
		}
		
		//Cut off the '0--' which will be added as part of the source node previous distance
		return path.substring(3);
	}

	private DjikstraNode getLeastDistanceNode(ArrayList<DjikstraNode> nodes) {
		int min_dist = Integer.MAX_VALUE;
		DjikstraNode min_node = null;
				
		for(DjikstraNode node : nodes) {
			if(node.getDist() < min_dist) {
				min_dist = node.getDist();
				min_node = node;
			}
		}
		return min_node;
	}

}
