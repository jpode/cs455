package cs455.overlay.wireformats;

public class EventFactory {
	//Static instance variable to follow singleton pattern
	private static EventFactory instance;
	
	//Threadsafe getter method
	public static synchronized EventFactory getInstance() {
		if(instance == null) {
			instance = new EventFactory();
		}
		return instance;
	}
	
	public Event createEvent(String data){		
		String[] lines = data.split("\n");
		int message_type = Integer.parseInt(lines[0]);
		
		switch(message_type) {
			case(0): //Register: node to registry
				return new Register(lines[1], Integer.parseInt(lines[2]));
			case(1): //Register response: registry to node
				return new RegisterResponse(lines[1].getBytes()[0], lines[2]);
			case(2): //Deregister: node to registry
				return new Deregister(lines[1], Integer.parseInt(lines[2]));
			case(3): //MessagingNodesList: registry to node
				MessagingNodesList ml = new MessagingNodesList(Integer.parseInt(lines[1]));

				for(int i = 2; i < lines.length; i++) {
					ml.addInfo(lines[i]);
				}
				return ml;
			case(4): //LinkWeights: registry to node
				LinkWeights lw = new LinkWeights(Integer.parseInt(lines[1]));

				for(int i = 2; i < lines.length; i++) {
					lw.addWeight(lines[i]);
				}
				return lw;
			case(5): //TaskInitiate: registry to node
				return new TaskInitiate(Integer.parseInt(lines[1]));
			case(6): //Message: node to node
				return new Message(lines[1], Integer.parseInt(lines[2]));
			case(7): //TaskComplete: node to registry
				return null;
			case(8): // PullTrafficSummary: registry to node
				return null;
			case(9): // TrafficSummary: node to registry
				return null;
			default:
				System.out.println("EventFactory::handlePacket: packet type unrecognized");
				return null;
		}
	}
}
