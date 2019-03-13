package cs455.scaling.tasks;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * AcceptConnectionTask is a task object that registers an incoming connection with the server, and is considered part of a "work unit" that
 *  will be assigned to  a worker thread by the ThreadPoolManager when a work unit is full or the batch time has expired.
 * Task workflow:
 *  - Acquire locks on the selector and server channel
 *  - Try to accept the connection into a channel
 *  - Configure the channel 
 *  - Register the channel with the server selector
 */
public class AcceptConnectionTask implements Task{
	private Selector selector;
	private ServerSocketChannel server_channel;
	private SelectionKey key;
	private final int client_id;
	
	public AcceptConnectionTask(SelectionKey key, Selector selector, ServerSocketChannel server_channel, int client_id) {
		this.selector = selector;
		this.server_channel = server_channel;
		this.key = key;
		this.client_id = client_id;
	}
	
	@Override
	public int getId() {
		return client_id;
	}
	
	@Override
	public void run() {
		
		//Acquire mutex locks on selector and server channel objects to avoid race conditions when registering the new connection
		synchronized(selector){
			synchronized(server_channel) {
				//Accept a new client socket, configure non-blocking mode, and register it for read operations
				SocketChannel client;
				try {
					client = server_channel.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ, client_id);
					((AtomicBoolean)key.attachment()).set(false);
				}catch(Exception e) {
					System.out.println("Client connection failure detected, cancelling channel key.");
					key.cancel();
				}
			}
		}
	}

}
