package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPServerThread implements Runnable{

	private boolean open;
	private ServerSocket ss;
	private ConcurrentLinkedQueue<Socket> queue;
	
	public TCPServerThread(Integer port, Integer num_connections) {
		//Cycle through ports until an empty one is found
		while(port < 66000) {
			try {
				//Test to see if the port is available
				Socket test_socket = new Socket("localhost", port);
				port++;

			} catch(IOException e) {
				//Create the server socket. Number of connections is specified as the max connection queue size
				try {
					this.ss = new ServerSocket(port, num_connections);
					queue = new ConcurrentLinkedQueue<Socket>();
					
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		this.open = false;
	}
	
	public Socket get() {
		return queue.poll();
	}
	
	public void run() {		
		while(open) {
			try {
				//Blocks until a connection comes in and is accepted 
				Socket client_socket = ss.accept();
				queue.add(client_socket);
				
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
