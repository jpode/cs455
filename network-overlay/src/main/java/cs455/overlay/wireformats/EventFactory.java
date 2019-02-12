package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.Socket;

import cs455.overlay.registry.Registry;

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
			case(1): //Register
				return new Register(lines[1], Integer.parseInt(lines[2]));
			case(2): //Deregister
				return new Deregister(lines[1], Integer.parseInt(lines[2]));
			case(3): //List messaging nodes
				MessagingNodesList ml = new MessagingNodesList(Integer.parseInt(lines[1]));

				for(int i = 2; i < lines.length; i++) {
					ml.addInfo(lines[i]);
				}
				return ml;
			case(4): //Assign link weights
				LinkWeights lw = new LinkWeights(Integer.parseInt(lines[1]));

				for(int i = 2; i < lines.length; i++) {
					lw.addWeight(lines[i]);
				}
				return lw;
			case(5): //Initiate send message task
				return null;
			case(6): //Send message
				return null;
			case(7): //Task completion
				return null;
			case(8): // Request traffic summaries
				return null;
			case(9): // Send traffic summary
				return null;
			default:
				System.out.println("EventFactory::handlePacket: packet type unrecognized");
				return null;
		}
	}
}
