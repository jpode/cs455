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
				//Test to see if the port is available
				Socket test_socket = new Socket("localhost", port);
				port++;

			} catch(IOException e) {
				//Create the server socket. Number of connections is specified as the max connection queue size
				try {
					this.ss = new ServerSocket(port);
					socket_queue = new ConcurrentLinkedQueue<Socket>();
					event_queue = new ConcurrentLinkedQueue<Event>();
					
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		this.open = false;
	}
	
	public Socket getSocket() {
		//This check is done to ensure that any thread polling for new socket connections will not retrieve the socket before the associated request message is available
		if(event_queue.size() == socket_queue.size()) {
			return socket_queue.poll();
		}
		return null;
	}
	
	public Event getRequest() {
		return event_queue.poll();
	}
	
	public void run() {		
		while(open) {
			try {
				//Blocks until a connection comes in and is accepted 
				Socket client_socket = ss.accept();
				
				//If a connection is received, there should be a request message immediately following it
				//Create a thread to listen for this, and an event to hold the request
				TCPReceiverThread request_listener = new TCPReceiverThread(client_socket);
				Event request_message;

				//Wait for 100 ms for one to come, otherwise discard the connection
				long startTime = System.currentTimeMillis(); //fetch starting time
				while(false||(System.currentTimeMillis()-startTime)<100) {
				   try {
					   request_message = request_listener.get();
					   
					   if(request_message != null) {
						   socket_queue.add(client_socket);
						   event_queue.add(request_message);
					   }
				   } catch (InterruptedException e) {
					   e.printStackTrace();
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
			System.out.println("ERROR: Server was not killed gracefully");
			e.printStackTrace();
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
