package cs455.overlay.djikstra;

import java.util.ArrayList;

import cs455.overlay.util.Connection;

public class DjikstraNode {
	private String node;
	private int distance;
	private int dist_to_previous;
	private DjikstraNode previous;
	private boolean visited;
	private ArrayList<DjikstraNode> adjacent_nodes;
	
	public DjikstraNode(String key) {
		node = key;
		distance = Integer.MAX_VALUE;
		previous = null;
		visited = false;
		adjacent_nodes = new ArrayList<DjikstraNode>();
	}
	
	public DjikstraNode(String key, int weight) {
		node = key;
		distance = weight;
		previous = null;
		adjacent_nodes = new ArrayList<DjikstraNode>();
	}
	
	public String getNode() {
		return node;
	}
	
	public int getDist() {
		return distance;
	}
	
	public DjikstraNode getPrev() {
		return previous;
	}
	
	public int getPrevDist() {
		return dist_to_previous;
	}
	
	public boolean visited() {
		return visited;
	}
	
	public void setDist(int dist) {
		distance = dist;
	}
	
	public void setPrev(DjikstraNode prev, int dist) {
		previous = prev;
		dist_to_previous = dist;
	}

	public void calculateAdjacencies(Connection[] connection_list) {
		for(Connection conn : connection_list) {
			if(conn.getFirstNode().toString().equals(node)) {
				adjacent_nodes.add(new DjikstraNode(conn.getSecondNode().toString(), conn.getWeight()));
			} else if(conn.getSecondNode().toString().equals(node)) {
				adjacent_nodes.add(new DjikstraNode(conn.getFirstNode().toString(), conn.getWeight()));
			}
		}
		
	}
	
	public DjikstraNode[] getAdjacencies() {
		return adjacent_nodes.toArray(new DjikstraNode[0]);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DjikstraNode))
			return false;
		DjikstraNode other = (DjikstraNode) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return node;
	}
}
