package cs455.overlay.wireformats;

public class LinkWeights {
	final int MESSAGE_TYPE = 5;
	int num_links;
	String[] link_weights;
	private int array_counter;
	
	public LinkWeights(int links) {
		num_links = links;
		link_weights = new String[links];
		array_counter = 0;
	}
	
	//Returns false if weight has already been added for all nodes, otherwise adds the weight and returns true
	public boolean addWeight(String weight) {
		if(array_counter < num_links) {
			link_weights[array_counter] = weight;
			array_counter++;
			return true;
		}
		return false;
	}
	
	public String getLinkWeights() {
		String weights = "";
		
		for(int i = 0; i < array_counter; i++) {
			weights += link_weights[i] + "\n";
		}
		
		return weights;
	}
}
