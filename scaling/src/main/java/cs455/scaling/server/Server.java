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
	private boolean running;
	private ThreadPoolManager manager;
	private Thread manager_thread;
	private int batch_size;
	
	public Server(int portnum, int thread_pool_size, int batch_size, int batch_time) {
		this.batch_size = batch_size;
		manager = new ThreadPoolManager(thread_pool_size, batch_size, batch_time);
		manager_thread = new Thread(manager);
		manager_thread.start();
	}

	public void startServer(int port) {
		try {
			System.out.println("Server: configuring...");
			//Object to attach to keys to prevent re-reading of the same key
			
			//Configure server selector and socket channel to listen for new connections
			Selector selector = Selector.open();
			ServerSocketChannel server_channel = ServerSocketChannel.open();
			
			server_channel.configureBlocking(false);
			server_channel.socket().bind(new InetSocketAddress("localhost", port));
			

			
			SelectionKey server_key = server_channel.register(selector,  SelectionKey.OP_ACCEPT);
			
			//Attach a boolean object to the key to prevent the server from sending duplicate registration tasks
			//When a new connection is received, the boolean is set to true until a worker thread completes the task and sets it back to false
			AtomicBoolean accepting_connection = new AtomicBoolean();
			accepting_connection.set(false);
			server_key.attach(accepting_connection);

			running = true;
			Iterator<SelectionKey> keys;
			System.out.println("Server: configuration done. Listening for connections...");
			//Server continuously accepts new connections
			while(running) {
				
				//Mutex locks do not need to be acquired for the server_channel and selector objects, as they are only modified in Tasks which acquire the locks
				//Block until there are keys with activity to handle
				selector.selectNow();
				
				//List of keys with activity to handle
				keys = selector.selectedKeys().iterator();
				while(keys.hasNext()) {
					//System.out.println("\t\tDEBUG::Server: num keys returned by select: " + selector.selectedKeys().size());

					SelectionKey key = keys.next();
					
					//System.out.println("\t\tDEBUG::Server: current key:: " + key.toString());

					if(key.isAcceptable()) {
						
						boolean check = accepting_connection.get();
						if(!check) {
							System.out.println("Server: accepted new client connection");
							
							//Set boolean attached to the key to true to prevent the same connection from being established twice
							accepting_connection.set(true);
							manager.execute(new AcceptConnectionTask(key, selector, server_channel));

						}
						//this.acceptConnection(selector, server_channel);
						
					} else if (key.isReadable()) {
						//If the key already contains an accepted client and is now readable, attempt to read it
						//this.read(key);
						manager.execute(new BatchTask(key));
					}
					
					//Remove the key so it does not get checked again 
					keys.remove();
				}
				selector.selectedKeys().clear();
				
				
			}
			
			System.out.println("Server: stopped running");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void acceptConnection(Selector selector, ServerSocketChannel server_channel) {
		SocketChannel client;
		try {
			client = server_channel.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read(SelectionKey key){
		try {
			//Read message from client to a buffer
			SocketChannel client = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(256);
			
			int bytes_read = client.read(buffer);
			if(bytes_read == -1) { // Connection has closed
				client.close();
				System.out.println("Client disconnected");
			} else {
				System.out.println("Server: read message from buffer: " + new String(buffer.array()).trim());
				buffer.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void write(SelectionKey key) {
		
	}
	
	public static void main(String[] args) {
		Server server = new Server(5001, 10, 5, 5);
		server.startServer(5001);
	}
}
