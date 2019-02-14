package cs455.overlay.wireformats;

public class MessagingNodesList implements Event{
	private final int MESSAGE_TYPE = 3;
	private int num_message_nodes;
	private String[] node_info;
	private int array_counter;
	
	
	public MessagingNodesList(int num_nodes) {
		num_message_nodes = num_nodes;
		node_info = new String[num_message_nodes];
		array_counter = 0;
	}
	
	//Returns false if info has already been added for all nodes, otherwise adds the info and returns true
	public boolean addInfo(String info) {
		if(array_counter < num_message_nodes) {
			node_info[array_counter] = info;
			array_counter++;
			return true;
		}
		return false;
	}
	
	public int getType() {
		return MESSAGE_TYPE;
	}
	
	public byte[] getBytes() {
		return new String(MESSAGE_TYPE + "\n" + num_message_nodes + "\n" + getNodeInfo()).getBytes();
	}
	
	private String getNodeInfo() {
		String nodeinfo = "";
		
		for(int i = 0; i < array_counter; i++) {
			nodeinfo += node_info[i] + "\n";
		}
		
		return nodeinfo;
	}
}
