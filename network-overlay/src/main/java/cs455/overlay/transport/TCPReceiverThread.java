package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPReceiverThread implements Runnable{
	private Socket socket;
	private DataInputStream din;
	private ConcurrentLinkedQueue<Event> queue;
	
	public TCPReceiverThread(Socket socket) throws IOException {
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
		queue = new ConcurrentLinkedQueue<Event>();
	}
	
	//Nonblocking call
	public Event get() throws InterruptedException {
		return queue.poll();
	}
	
	public void run() {
		int dataLength;
		while (socket != null) {
			try {
				//Blocks until a message comes in
				dataLength = din.readInt();
				byte[] data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
				
				//Add a new event created from the message to the queue
				queue.add(EventFactory.getInstance().createEvent(data.toString()));
				
			} catch (SocketException se) {
				System.out.println(se.getMessage());
				break;
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage()) ;
				break;
			}
		}
	}
	
	public void kill() {
		try {
			din.close();
			socket.close();
			socket = null;
			queue.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
