package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.scaling.tasks.AcceptConnectionTask;
import cs455.scaling.tasks.BatchTask;

public class Server {
	//Server port number
	private int port;
	//Runnable object that controls the ThreadPool and assignment of tasks
	//Requires a thread pool size, batch size, and batch time in seconds specified by the user
	private ThreadPoolManager manager;
	//Thread to run the manager object
	private Thread manager_thread;
	
	public Server(int port, int thread_pool_size, int batch_size, int batch_time) {
		this.port = port;
		manager = new ThreadPoolManager(thread_pool_size, batch_size, batch_time);
		manager_thread = new Thread(manager);

	}

	public void startServer() {
		try {
			System.out.println("Server: configuring...");
			
			//Object to attach to keys to prevent re-reading of the same key
			
			//Configure server selector and socket channel to listen for new connections
			Selector selector = Selector.open();
			ServerSocketChannel server_channel = ServerSocketChannel.open();
			
			server_channel.configureBlocking(false);
			server_channel.socket().bind(new InetSocketAddress("0.0.0.0", port));
			

			
			SelectionKey server_key = server_channel.register(selector,  SelectionKey.OP_ACCEPT);
			
			//Attach a boolean object to the key to prevent the server from trying to complete duplicate AcceptConnectionTasks
			//When a new connection is received, the boolean is set to true until a worker thread completes the task and sets it back to false
			//The boolean is not added to the client key, as a new object is assigned to keep track of statistics
			AtomicBoolean active = new AtomicBoolean();
			active.set(false);
			server_key.attach(active);
			
			//Start the thread pool manager
			manager_thread.start();
			
			//Declare keys iterator, which will loop through keys with activity
			Iterator<SelectionKey> keys;
			
			//To keep track of each client's message statistics, an incremented id will be attached to the key object
			int client_id_counter = 0;
			
			System.out.println("Server: configuration done. Listening started...");
			//Server continuously accepts new connections
			while(true) {
				
				if(selector.selectNow() != 0) {
					
					//List of keys with activity to handle
					keys = selector.selectedKeys().iterator();
					while(keys.hasNext()) {
	
						SelectionKey key = keys.next();
	
						if(!key.isValid()) {
							continue;
						}
						
						if(key.isAcceptable()) { //Indicates a new client connection that needs to be accepted
							
							//Check to see if this client is already trying to connect
							boolean check = active.get();
							if(!check) {
								
								//Set boolean attached to the key to true to prevent the same connection from being established twice
								active.set(true);
								manager.execute(new AcceptConnectionTask(key, selector, server_channel, client_id_counter));
								
								client_id_counter++;
	
							}
							
						} else if (key.isReadable()) { //Indicates that an existing client has sent a message that needs to be handled
							manager.execute(new BatchTask(key, selector));
						}
						
						//Remove the key so it does not get checked again in this iteration
						keys.remove();
					}
					//Clear all keys to ensure a new set for the next iteration
					selector.selectedKeys().clear();
				}
			}
			
			
		} catch (IOException e) {
			manager_thread.interrupt();
			System.out.println("Server: stopped running");
			return;
		}
		
	}
	
	public static void main(String[] args) {
		if(args.length != 4) {
			System.out.println("Incorrect number of arguments - 4 required");
			return;
		}
		
		Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		server.startServer();
	}
}
