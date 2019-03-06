package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.overlay.wireformats.Event;

public class TCPServerThread implements Runnable{

	private boolean open;
	private ServerSocket ss;
	private ConcurrentLinkedQueue<Socket> socket_queue;
	private ConcurrentLinkedQueue<TCPReceiverThread> rec_queue;
	private ConcurrentLinkedQueue<Thread> thd_queue;
	private ConcurrentLinkedQueue<Event> event_queue;
	
	//For registry, where the port should be whatever was specified
	public TCPServerThread(Integer port) {
		try {
			this.ss = new ServerSocket(port);
			
			System.out.println("Server thread started on " + ss.getInetAddress() + ":" + ss.getLocalPort());

			socket_queue = new ConcurrentLinkedQueue<Socket>();
			event_queue = new ConcurrentLinkedQueue<Event>();
			rec_queue = new ConcurrentLinkedQueue<TCPReceiverThread>();
			thd_queue = new ConcurrentLinkedQueue<Thread>();
			
			open = true;
		} catch (IOException e1) {
			System.out.println("ERR: Could not start server thread on the specified port.");
			this.open = false;
			System.exit(1);
		}
	}
	
	//For messaging node, where it doesn't matter what the port is
	public TCPServerThread() {
		//Cycle through ports until an empty one is found
		int port = 5000;
		while(port < 66000) {
			try {
				this.ss = new ServerSocket(port);
				
				System.out.println("Server thread started on " + ss.getInetAddress() + ":" + ss.getLocalPort());

				socket_queue = new ConcurrentLinkedQueue<Socket>();
				event_queue = new ConcurrentLinkedQueue<Event>();
				
				open = true;
				break;
			} catch (IOException e1) {
				port++;
				continue;
			}
		}
		
		this.open = false;
	}
	
	public int getPort() {
		return ss.getLocalPort();
	}
	
	public Socket getSocket() {
		//This check is done to ensure that any thread polling for new socket connections will not retrieve the socket before the associated request message and data input stream is available
		if(event_queue.size() == socket_queue.size() && rec_queue.size() == socket_queue.size() && thd_queue.size() == socket_queue.size()) {
			return socket_queue.poll();
		}
		return null;
	}
	
	public TCPReceiverThread getReciever() {
		return rec_queue.poll();
	}
	
	public Thread getThread() {
		return thd_queue.poll();
	}
	
	public Event getEvent() {
		return event_queue.poll();
	}
	
	public void run() {	
		while(open) {
			try {
				//System.out.println("TCPServerThread::run: waiting for new connection...");
				
				//Blocks until a connection comes in and is accepted 
				Socket client_socket = ss.accept();
				//Create the DataInputStream here because it will need to be passed back up to the registry before being closed, which would permanently close the input side of the socket itself
				DataInputStream din = new DataInputStream(client_socket.getInputStream()); 
				
				//System.out.println("TCPServerThread::run: received new connection");
				
				//If a connection is received, there should be a message (of type Register or Message) immediately following it
				//Create a thread to listen for this, and an event to hold the message
				TCPReceiverThread message_listener = new TCPReceiverThread(client_socket, din);
				Thread message_thread = new Thread(message_listener);
				message_thread.start();
				
				Event message;

				//Wait for 200 ms for initial message to come, otherwise discard the connection
				long startTime = System.currentTimeMillis(); //fetch starting time
				while(false||(System.currentTimeMillis()-startTime)<200) {
				   try {
					   message = message_listener.get();
					   
					   if(message != null) {
						   event_queue.add(message);
						   socket_queue.add(client_socket);
						   rec_queue.add(message_listener);
						   thd_queue.add(message_thread);
					   }
				   } catch (InterruptedException e) {
					   //System.out.println("TCPServerThread::run: error receiving message");
				   }
				}
					
				//Kill the temporary listener and thread but do not close the socket, as new listeners/threads will be created with the socket
				message_listener.kill();
				message_thread.interrupt();
			} catch (SocketException se) {
				se.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public void killServer() {
		open = false;
		try {
			ss.close();
		} catch (IOException e) {
			System.out.println("ERROR: Server object was not killed gracefully");
		}
	}

	public int serverStatus() {
		//Returns an int instead of boolean for probable future use, where the server will return a more detailed status
		if(open) {
			return 0;
		} else {
			return 1;
		}
	}
}
