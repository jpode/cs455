package cs455.overlay.transport;

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
	private ConcurrentLinkedQueue<Event> event_queue;
	
	public TCPServerThread(Integer port) {
		//Cycle through ports until an empty one is found
		while(port < 66000) {
			try {
				this.ss = new ServerSocket(port);
				
				System.out.println("Server thread started: " + ss.getInetAddress() + ":" + ss.getLocalPort());

				socket_queue = new ConcurrentLinkedQueue<Socket>();
				event_queue = new ConcurrentLinkedQueue<Event>();
				
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
		//This check is done to ensure that any thread polling for new socket connections will not retrieve the socket before the associated request message is available
		if(event_queue.size() == socket_queue.size()) {
			return socket_queue.poll();
		}
		return null;
	}
	
	public Event getEvent() {
		return event_queue.poll();
	}
	
	public void run() {	
		open = true;
		while(open) {
			try {
				//Blocks until a connection comes in and is accepted 
				System.out.println("TCPServerThread::run: waiting for new connection...");

				Socket client_socket = ss.accept();
				
				System.out.println("TCPServerThread::run: received new connection");
				//If a connection is received, there should be a message (of type Register or Message) immediately following it
				//Create a thread to listen for this, and an event to hold the message
				TCPReceiverThread request_listener = new TCPReceiverThread(client_socket);
				Thread request_thread = new Thread(request_listener);
				request_thread.start();
				
				Event message;

				//Wait for 100 ms for one to come, otherwise discard the connection
				long startTime = System.currentTimeMillis(); //fetch starting time
				while(false||(System.currentTimeMillis()-startTime)<100) {
				   try {
					   message = request_listener.get();
					   
					   if(message != null) {
						   socket_queue.add(client_socket); //Socket isn't needed if the message is not a Register request, but the parent thread can dispose of it anyways
						   event_queue.add(message);
					   }
				   } catch (InterruptedException e) {
					   System.out.println("TCPServerThread::run: error receiving message");
				   }
				}
								
			} catch (SocketException se) {
				System.out.println(se.getMessage());
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage()) ;
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
