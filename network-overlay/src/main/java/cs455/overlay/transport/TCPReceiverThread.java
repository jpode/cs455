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
	private boolean listening;
	
	public TCPReceiverThread(Socket socket) throws IOException {
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
		queue = new ConcurrentLinkedQueue<Event>();
		listening = true;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	//Nonblocking call
	public Event get() throws InterruptedException {
		return queue.poll();
	}
	
	public boolean isListening() {
		return listening;
	}
	
	public void run() {
		int dataLength;
		System.out.println("TCPReceiverThread::run: starting...");
		while (socket != null) {
			try {
				//Blocks until a message comes in
				dataLength = din.readInt();
				System.out.println("TCPReceiverThread::run: received message from " + socket.getRemoteSocketAddress().toString().substring(1));
				byte[] data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
				//System.out.println("Received message: \n" + new String(data));
				//Add a new event created from the message to the queue
				queue.add(EventFactory.getInstance().createEvent(new String(data)));
				
			} catch (SocketException se) {
				System.out.println(se.getMessage());
				break;
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage()) ;
				break;
			}
		}
		
		System.out.println("TCPReceiverThread::run: stopped running");
		listening = false;
	}
	
	public void kill() {
		try {
			din.close();
			socket.close();
			socket = null;
			queue.clear();
			listening = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
