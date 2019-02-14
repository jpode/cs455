package cs455.overlay.wireformats;

public class LinkWeights implements Event{
	private final int MESSAGE_TYPE = 4;
	private int num_links;
	private String[] link_weights;
	private int array_counter;
	
	public LinkWeights(int links) {
		link_weights = new String[links];
		num_links = links;
		array_counter = 0;
	}
	
	public int getType() {
		return MESSAGE_TYPE;
	}
	
	public byte[] getBytes() {
		return new String(MESSAGE_TYPE + "\n" + num_links + "\n" + getLinkWeights()).getBytes();
	}
	
	//Add a link and its weight to the list
	public void addWeight(String weight) {
		if(array_counter < num_links) {
			link_weights[array_counter] = weight;
			array_counter++;
		}
	}
	
	//Create a string of all the weights
	private String getLinkWeights() {
		String weights = "";
		
		for(int i = 0; i < num_links; i++) {
			weights += link_weights[i] + "\n";
		}
		
		return weights;
	}
}
